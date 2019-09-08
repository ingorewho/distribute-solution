package com.distribute.common.redis.support.lock;

import com.distribute.common.redis.RedisKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisCommands;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author renzhiqiang
 * @Description redis锁(乐观锁和悲观锁)
 * @Date 2019-08-22
 **/
@Service
public class RedisDistLock implements DistLock {
    private static final Logger LOG = LoggerFactory.getLogger(RedisDistLock.class);
    public static final String UNLOCK_LUA;
    private ThreadLocal<String> valueCache = new ThreadLocal();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    static {
        StringBuilder sb = new StringBuilder();
        sb.append("if redis.call(\"get\",KEYS[1]) == ARGV[1] ");
        sb.append("then ");
        sb.append("    return redis.call(\"del\",KEYS[1]) ");
        sb.append("else ");
        sb.append("    return 0 ");
        sb.append("end ");
        UNLOCK_LUA = sb.toString();
    }

    /**
     * redis乐观锁实现：
     * 在一个session中，通过watch命令监控一个key，通过multi命令开启事务，
     *
     */
    @Override
    public boolean casLock(RedisKey redisKey) {
        SessionCallback<List<Boolean>> callback = new SessionCallback<List<Boolean>>() {
            @Override
            public List<Boolean> execute(RedisOperations operations) throws DataAccessException {
                // redis监控一个key
                operations.watch(redisKey.getKey());
                // 开始事务
                operations.multi();
                operations.opsForValue().set(redisKey.getKey(), UUID.randomUUID());
                operations.expire(redisKey.getKey(), redisKey.getExpireTime(), TimeUnit.MILLISECONDS);
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

    @Override
    public boolean lock(RedisKey redisKey) {
        return lock(redisKey.getKey(), redisKey.getExpireTime(), redisKey.getMaxRetryCounts(), redisKey.getSleepTime());
    }


    /**
     * 加锁
     * @param key 键key
     * @param expireInMillis 过期时间
     * @param maxRetryCounts 最大重试次数
     * @param sleepInMillis 重试间隔时间
     * @return
     */
    private boolean lock(String key, long expireInMillis, int maxRetryCounts, long sleepInMillis) {
        if (expireInMillis <= 0) {
            expireInMillis = MIN_EXPIRE_TIME_IN_MILLIS;
        }

        if (maxRetryCounts <= 0) {
            maxRetryCounts = DEFAULT_RETRY_COUNT;
        }

        if (sleepInMillis <= 0) {
            sleepInMillis = DEFAULT_SLEEP_IN_MILLIS;
        }

        boolean result;
        for(result = this.setNxPx(key, expireInMillis); !result && maxRetryCounts-- > 0;) {
            try {
                result = this.setNxPx(key, expireInMillis);
                TimeUnit.MILLISECONDS.sleep(sleepInMillis);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
        return result;
    }

    /**
     * EX seconds – 设置键key的过期时间，单位时秒
     * PX milliseconds – 设置键key的过期时间，单位时毫秒
     * NX – 只有键key不存在的时候才会设置key的值
     * XX – 只有键key存在的时候才会设置key的值
     *
     * @param key
     * @param expireInMillis
     * @return
     */
    private boolean setNxPx(String key, long expireInMillis) {
        try {
            String result = (String)this.redisTemplate.execute(new RedisCallback<String>() {
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                    JedisCommands commands = (JedisCommands)connection.getNativeConnection();
                    String lockValue = UUID.randomUUID().toString();
                    valueCache.set(lockValue);
                    // 可以保证原子性
                    return commands.set(key, lockValue, "NX", "PX", expireInMillis);
                }
            });
            return StringUtils.isNotEmpty(result);
        } catch (Exception ex) {
            LOG.error("set nx px key : {} failed", key, ex);
            return false;
        }
    }


    @Override
    public boolean unlock(RedisKey redisKey) {
        try {
            final List<String> keys = Collections.singletonList(redisKey.getKey());
            final List<String> args = Collections.singletonList(this.valueCache.get());
            Long result = (Long)this.redisTemplate.execute(new RedisCallback<Long>() {
                public Long doInRedis(RedisConnection connection) throws DataAccessException {
                    Object nativeConnection = connection.getNativeConnection();
                    if (nativeConnection instanceof JedisCluster) {
                        return (Long)((JedisCluster)nativeConnection).eval(UNLOCK_LUA, keys, args);
                    } else {
                        return nativeConnection instanceof Jedis ? (Long)((Jedis)nativeConnection).eval(RedisDistLock.UNLOCK_LUA, keys, args) : 0L;
                    }
                }
            });
            return result != null && result > 0L;
        } catch (Exception ex) {
            LOG.error("unlock: {} failed", redisKey, ex);
            return false;
        }
    }
}
