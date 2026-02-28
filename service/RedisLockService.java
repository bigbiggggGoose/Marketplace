package com.example.marketplace.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class RedisLockService {

    private final RedissonClient redissonClient;

    public RedisLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public <T> T executeWithLock(String key,
                                 long waitTime,
                                 long leaseTime,
                                 TimeUnit unit,
                                 Supplier<T> supplier) {
        RLock lock = redissonClient.getLock(key);
        boolean locked = false;
        try {
            locked = lock.tryLock(waitTime, leaseTime, unit);
            if (!locked) {
                throw new IllegalStateException("获取分布式锁失败：" + key);
            }
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取分布式锁被中断：" + key, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}

