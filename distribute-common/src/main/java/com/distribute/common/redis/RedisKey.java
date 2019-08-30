package com.distribute.common.redis;

/**
 * @Author renzhiqiang
 * @Description redis存储对象
 * @Date 2019-08-22
 **/
public class RedisKey {
    private String key;

    /**
     * 单位：毫秒
     */
    private Long expireTime;

    private Long sleepTime;

    private Integer maxRetryCounts;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

    public Long getSleepTime() {
        return sleepTime;
    }

    public void setSleepTime(Long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public Integer getMaxRetryCounts() {
        return maxRetryCounts;
    }

    public void setMaxRetryCounts(Integer maxRetryCounts) {
        this.maxRetryCounts = maxRetryCounts;
    }

    public RedisKey(String key, Long expireTime, Long sleepTime, Integer maxRetryCounts) {
        this.key = key;
        this.expireTime = expireTime;
        this.sleepTime = expireTime;
        this.maxRetryCounts = maxRetryCounts;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        private String key;

        private Long expireTime;

        private Long sleepTime;

        private Integer maxRetryCounts;

        public Builder key(String key) {
            this.key = key;
            return this;
        }


        public Builder expireTime(Long expireTime) {
            this.expireTime = expireTime;
            return this;
        }

        public Builder sleepTime(Long sleepTime) {
            this.sleepTime = sleepTime;
            return this;
        }

        public Builder maxRetryCounts(Integer maxRetryCounts) {
            this.maxRetryCounts = maxRetryCounts;
            return this;
        }

        public RedisKey build() {
            return new RedisKey(key, expireTime, sleepTime, maxRetryCounts);
        }
    }
}
