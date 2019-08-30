package com.distribute.replay.base.aspect;

import com.distribute.common.redis.RedisKey;
import com.distribute.common.redis.support.lock.DistLock;
import com.distribute.common.response.ApiResponse;
import com.distribute.common.util.JsonUtil;
import com.distribute.common.util.Md5Util;
import com.distribute.replay.base.annotation.Replay;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author renzhiqiang
 * @Description 重复请求切面处理
 * @Date 2019-08-21
 **/
@Aspect
@Component
public class ReplayAspect {
    private Logger logger = LoggerFactory.getLogger(ReplayAspect.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private DistLock distLock;

    /**
     * 处理重复请求：
     * 思路：
     * 1.根据参数和方法名格式化成json对象，然后进行md5加密，保证相同参数能够计算得到相同的md5值
     * 2.md5值作为key，尝试去redis中获取对应数据，如果存在说明是重复请求，如果不存在则使用redis乐观锁去set数据，set成功执行业务逻辑，
     *   set失败说明存在并发，仍认为是重复请求
     * @param point
     * @param replay
     * @return
     * @throws Throwable
     */
    @Around("@annotation(replay)")
    public Object handReplay(ProceedingJoinPoint point, Replay replay) throws Throwable {
        //获取方法实例
        Signature signature = point.getSignature();
        //获取调用方法全名称:包名.类名.方法名
        String caller = signature.toLongString();
        //参数
        Object[] args = point.getArgs();
        Object response = null;
        StopWatch watch = new StopWatch();

        try {
            watch.start();
            // 判断是否需要防重
            if (!replay.enable()) {
                Map<String, Object> map = new HashMap<>(2);
                map.put("caller", caller);
                map.put("args", args);
                String cacheKey = Md5Util.md5(JsonUtil.obj2json(map));
                boolean isReplayReq = repalyAction(replay, cacheKey);
                if (isReplayReq) {
                    return ApiResponse.buildFailed("请勿重复提交.");
                }
            }
            response = point.proceed();
            return response;
        } catch (Throwable ex) {
            String msg = String.format("caller:{}, param:{}, occur exception.", caller, args);
            logger.error(msg, ex);
            return ApiResponse.buildFailed(msg);
        } finally {
            watch.stop();
            logger.info("caller:{}, param:{}, return:{}, use time:{}ms", caller, args, response, watch.getTotalTimeSeconds());
        }
    }

    /**
     * 判读是否重复请求
     *
     * @param replay
     * @param cacheKey
     * @return
     */
    private boolean repalyAction(Replay replay, String cacheKey) {

        if (redisTemplate.hasKey(cacheKey)) {
            return true;
        } else {
            RedisKey redisKey = RedisKey.newBuilder()
                    .key(cacheKey)
                    .expireTime(replay.expireTime())
                    .build();
            return distLock.casLock(redisKey);
        }
    }

}
