package com.monowear.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Blacklist cho Reset Token (JWT jti).
 * Đảm bảo mỗi Reset Token chỉ dùng được đúng 1 lần.
 */
@Component
@Slf4j
public class ResetTokenBlacklist {

    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    public ResetTokenBlacklist() {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "reset-token-blacklist-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::cleanup, 20, 20, TimeUnit.MINUTES);
    }

    public void invalidate(String jti, long expiryEpochSec) {
        blacklist.put(jti, expiryEpochSec);
        log.debug("Reset token blacklisted: jti={}", jti);
    }

    public boolean isBlacklisted(String jti) {
        return blacklist.containsKey(jti);
    }

    private void cleanup() {
        long now = System.currentTimeMillis() / 1000;
        int removed = 0;
        for (var it = blacklist.entrySet().iterator(); it.hasNext(); ) {
            if (it.next().getValue() < now) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("ResetTokenBlacklist cleanup: removed {} expired entries", removed);
        }
    }
}
