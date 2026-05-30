package com.monowear.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * In-memory OTP store với TTL tự động expire.
 * Thread-safe dùng ConcurrentHashMap.
 */
@ApplicationScoped
public class OtpStore {

    private static final Logger LOG = Logger.getLogger(OtpStore.class);

    private record OtpEntry(String code, Instant expiresAt) {
        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    private final ConcurrentHashMap<String, OtpEntry> store = new ConcurrentHashMap<>();

    public OtpStore() {
        // Cleanup thread mỗi 10 phút để tránh memory leak
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "otp-store-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::cleanup, 10, 10, TimeUnit.MINUTES);
    }

    /**
     * Lưu OTP cho email với TTL (tính bằng phút).
     */
    public void save(String email, String code, int ttlMinutes) {
        store.put(email.toLowerCase(), new OtpEntry(code, Instant.now().plusSeconds(ttlMinutes * 60L)));
        LOG.debugf("OTP saved for %s (TTL: %d min)", email, ttlMinutes);
    }

    /**
     * Kiểm tra OTP có hợp lệ không (không tự xóa — gọi remove() sau khi verify thành công).
     */
    public boolean verify(String email, String code) {
        OtpEntry entry = store.get(email.toLowerCase());
        if (entry == null) return false;
        if (entry.isExpired()) {
            store.remove(email.toLowerCase());
            return false;
        }
        return entry.code().equals(code);
    }

    /**
     * Xóa OTP sau khi đã dùng (prevent replay).
     */
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
            LOG.debugf("OtpStore cleanup: removed %d expired entries", removed);
        }
    }
}
