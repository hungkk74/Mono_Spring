package com.monowear.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * In-memory OTP store với TTL tự động expire.
 * Thread-safe dùng ConcurrentHashMap.
 */
@Component
@Slf4j
public class OtpStore {

    private record OtpEntry(String code, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();

    public OtpStore() {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "otp-store-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::cleanup, 10, 10, TimeUnit.MINUTES);
    }

    public void save(String email, String code, int ttlMinutes) {
        store.put(email.toLowerCase(), new OtpEntry(code, Instant.now().plusSeconds(ttlMinutes * 60L)));
        log.debug("OTP saved for {} (TTL: {} min)", email, ttlMinutes);
    }

    public boolean verify(String email, String code) {
        OtpEntry entry = store.get(email.toLowerCase());
        if (entry == null) return false;
        if (entry.isExpired()) {
            store.remove(email.toLowerCase());
            return false;
        }
        return entry.code().equals(code);
    }

    public void remove(String email) {
        store.remove(email.toLowerCase());
    }

    private void cleanup() {
        int removed = 0;
        for (var it = store.entrySet().iterator(); it.hasNext(); ) {
            if (it.next().getValue().isExpired()) {
                it.remove();
                removed++;
            }
        }
        if (removed > 0) {
            log.debug("OtpStore cleanup: removed {} expired entries", removed);
        }
    }
}
