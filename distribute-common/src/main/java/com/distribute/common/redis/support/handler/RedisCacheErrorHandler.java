package com.distribute.common.redis.support.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * @Author: renzhiqiang
 * @Description: redis缓存错误处理器
 * @Date: 2019/1/12
 */
public class RedisCacheErrorHandler implements CacheErrorHandler {
    private Logger logger = LogManager.getLogger();
    @Override
    public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
        logger.error("执行redis-get异常, key:{}", key, e);
    }

    @Override
    public void handleCachePutError(RuntimeException e, Cache cache, Object key, Object value) {
        logger.error("执行redis-put异常, 缓存对象:key={},value={}", key, value, e);
    }

    @Override
    public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
        logger.error("执行redis-evic异常, key:{}", key, e);
    }

    @Override
    public void handleCacheClearError(RuntimeException e, Cache cache) {
        logger.error("执行redis-clear异常", e);
    }
}
