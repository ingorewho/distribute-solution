package com.distribute.replay.base.aspect;

import com.distribute.common.redis.base.RedisKeyValue;
import com.distribute.common.redis.support.lock.RedisLock;
import com.distribute.common.response.ApiResponse;
import com.distribute.common.util.JsonUtil;
import com.distribute.replay.base.annotation.Replay;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author renzhiqiang
 * @Description 重复请求切面处理
 * @Date 2019-08-21
 **/
@Aspect
@Component
public class ReplayAspect {
    private Logger logger = LoggerFactory.getLogger(ReplayAspect.class);

    private static final String CACHE_VALUE = "value";

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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
                String cacheKey = JsonUtil.obj2json(map);
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
            RedisKeyValue redisKeyValue = RedisKeyValue.newBuilder()
                    .key(cacheKey)
                    .value("value")
                    .expireTime(replay.expireTime())
                    .build();
            return RedisLock.operateWithCasLock(redisKeyValue, redisTemplate);
        }
    }

}
