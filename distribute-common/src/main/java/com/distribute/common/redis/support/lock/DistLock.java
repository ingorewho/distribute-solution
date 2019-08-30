package com.distribute.common.redis.support.lock;

import com.distribute.common.redis.RedisKey;

/**
 * @Author renzhiqiang
 * @Description 分布式锁
 * @Date 2019-08-29
 **/
public interface DistLock {
    int DEFAULT_RETRY_COUNT = 0;
    int DEFAULT_SLEEP_IN_MILLIS = 50;
    int MIN_EXPIRE_TIME_IN_MILLIS = 500;

    /**
     * 乐观锁：加锁
     * 加锁方式：
     * 1.在一个session中，通过watch命令监控一个key，通过multi命令开启事务，执行一系列命令
     * 2.如果执行过程中，某个命令被其他线程执行了则整体执行失败，返回null
     * @param redisKey
     * @return
     */
    boolean casLock(RedisKey redisKey);

    /**
     * 悲观锁：加锁
     * 加锁方式：使用nx px命令搭配组合进行加锁，可以保证原子性
     * 遵守原则：
     * 1.谁加锁必须由谁来解锁，即同一个线程加锁、解锁，常用手动：value设置一个随机字符串，解锁时也根据key+value进行解锁
     * 2.锁超时时间必须大于业务执行结束时间，避免出现并发问题
     * @param redisKey
     * @return
     */
    boolean lock(RedisKey redisKey);

    /**
     * 悲观锁：解锁
     * 解锁方式：使用jedis lua脚本语言执行解锁命令
     * @param redisKey
     * @return
     */
    boolean unlock(RedisKey redisKey);
}
