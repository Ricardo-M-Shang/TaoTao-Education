package com.taotao.education.chat.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis 分布式锁工具类
 * 用于防止重复操作（如重复创建聊天室、重复发送邀请等）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLockUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LOCK_PREFIX = "lock:";
    private static final long DEFAULT_LOCK_TIMEOUT = 30; // 默认锁超时时间（秒）
    private static final long DEFAULT_WAIT_TIMEOUT = 10; // 默认等待超时时间（秒）

    // Lua 脚本：安全释放锁（只有持有者才能释放）
    private static final String UNLOCK_SCRIPT = """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            else
                return 0
            end
            """;

    /**
     * 尝试获取锁
     *
     * @param lockKey   锁的 key
     * @param timeout   锁超时时间
     * @param unit      时间单位
     * @return 锁的唯一标识（用于释放锁），获取失败返回 null
     */
    public String tryLock(String lockKey, long timeout, TimeUnit unit) {
        String key = LOCK_PREFIX + lockKey;
        String lockValue = UUID.randomUUID().toString();

        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, lockValue, timeout, unit);
        if (Boolean.TRUE.equals(success)) {
            log.debug("获取锁成功: key={}", key);
            return lockValue;
        }
        log.debug("获取锁失败: key={}", key);
        return null;
    }

    /**
     * 尝试获取锁（使用默认超时时间）
     */
    public String tryLock(String lockKey) {
        return tryLock(lockKey, DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * 自旋等待获取锁
     *
     * @param lockKey     锁的 key
     * @param lockTimeout 锁超时时间（秒）
     * @param waitTimeout 等待超时时间（秒）
     * @return 锁的唯一标识，获取失败返回 null
     */
    public String lockWithWait(String lockKey, long lockTimeout, long waitTimeout) {
        long startTime = System.currentTimeMillis();
        long waitMillis = waitTimeout * 1000;

        while (System.currentTimeMillis() - startTime < waitMillis) {
            String lockValue = tryLock(lockKey, lockTimeout, TimeUnit.SECONDS);
            if (lockValue != null) {
                return lockValue;
            }
            try {
                Thread.sleep(100); // 等待 100ms 后重试
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }
        return null;
    }

    /**
     * 释放锁
     *
     * @param lockKey   锁的 key
     * @param lockValue 锁的唯一标识
     * @return 是否成功释放
     */
    public boolean unlock(String lockKey, String lockValue) {
        if (lockValue == null) {
            return false;
        }

        String key = LOCK_PREFIX + lockKey;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(UNLOCK_SCRIPT, Long.class);
        Long result = redisTemplate.execute(script, Collections.singletonList(key), lockValue);

        boolean success = Long.valueOf(1L).equals(result);
        if (success) {
            log.debug("释放锁成功: key={}", key);
        } else {
            log.warn("释放锁失败（锁不存在或已被其他线程持有）: key={}", key);
        }
        return success;
    }

    /**
     * 在锁保护下执行操作
     *
     * @param lockKey  锁的 key
     * @param supplier 要执行的操作
     * @param <T>      返回类型
     * @return 操作结果
     * @throws LockAcquireException 获取锁失败时抛出
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> supplier) {
        String lockValue = tryLock(lockKey);
        if (lockValue == null) {
            throw new LockAcquireException("获取锁失败，请稍后重试");
        }
        try {
            return supplier.get();
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /**
     * 在锁保护下执行操作（无返回值）
     */
    public void executeWithLock(String lockKey, Runnable runnable) {
        executeWithLock(lockKey, () -> {
            runnable.run();
            return null;
        });
    }

    /**
     * 在锁保护下执行操作（带等待）
     */
    public <T> T executeWithLockAndWait(String lockKey, long waitTimeout, Supplier<T> supplier) {
        String lockValue = lockWithWait(lockKey, DEFAULT_LOCK_TIMEOUT, waitTimeout);
        if (lockValue == null) {
            throw new LockAcquireException("等待获取锁超时，请稍后重试");
        }
        try {
            return supplier.get();
        } finally {
            unlock(lockKey, lockValue);
        }
    }

    /**
     * 防重复提交检查
     *
     * @param key     唯一标识（如 userId:action:targetId）
     * @param seconds 防重时间（秒）
     * @return true 表示可以执行，false 表示重复提交
     */
    public boolean checkAndPreventDuplicate(String key, long seconds) {
        String redisKey = "duplicate:" + key;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", seconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * 锁获取异常
     */
    public static class LockAcquireException extends RuntimeException {
        public LockAcquireException(String message) {
            super(message);
        }
    }
}

