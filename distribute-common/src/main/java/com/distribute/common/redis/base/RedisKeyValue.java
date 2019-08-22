package com.distribute.common.redis.base;

/**
 * @Author renzhiqiang
 * @Description redis存储对象
 * @Date 2019-08-22
 **/
public class RedisKeyValue<K, V> {
    private K key;

    private V value;

    private Long expireTime;

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public RedisKeyValue(K key, V value, Long expireTime) {
        this.key = key;
        this.value = value;
        this.expireTime = expireTime;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder<K, V> {
        private K key;

        private V value;

        private Long expireTime;

        public Builder key(K key) {
            this.key = key;
            return this;
        }

        public Builder value(V value) {
            this.value = value;
            return this;
        }

        public Builder expireTime(Long expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        public RedisKeyValue build() {
            return new RedisKeyValue(key, value, expireTime);
        }
    }
}
