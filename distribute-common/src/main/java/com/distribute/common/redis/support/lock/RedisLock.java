package com.distribute.common.redis.support.lock;

import com.distribute.common.redis.base.RedisKeyValue;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author renzhiqiang
 * @Description redis锁(乐观锁和悲观锁)
 * @Date 2019-08-22
 **/
public class RedisLock {

    /**
     * 使用乐观锁方式存储数据：
     * redis乐观锁实现：在一个session中，通过watch命令监控一个key，通过multi命令开启事务，
     * 执行一系列命令，如果过程中，这些命令中某个命令被其他线程执行了则整体执行失败，执行返回null
     *
     * @param keyValue
     * @param redisTemplate
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> boolean operateWithCasLock(RedisKeyValue<K, V> keyValue, RedisTemplate<K, V> redisTemplate) {
        SessionCallback<List<Boolean>> callback = new SessionCallback<List<Boolean>>() {
            @Override
            public List<Boolean> execute(RedisOperations operations) throws DataAccessException {
                // redis监控一个key
                operations.watch(keyValue.getKey());
                // 开始事务
                operations.multi();
                operations.opsForValue().set(keyValue.getKey(), keyValue.getValue());
                operations.expire(keyValue.getKey(), keyValue.getExpireTime(), TimeUnit.MILLISECONDS);
                // 执行命令并结束事务
                return operations.exec();
            }
        };
        List<Boolean> result = redisTemplate.execute(callback);
        if (CollectionUtils.isEmpty(result)) {
            //操作失败，可能存在并发，redis数据被其他线程修改了，这时认为是重复请求
            return true;
        } else {
            //操作成功，这时可以认为是非重复请求
            return false;
        }
    }
}
