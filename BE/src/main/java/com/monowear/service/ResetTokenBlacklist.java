package com.monowear.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Blacklist cho Reset Token (JWT jti).
 * Đảm bảo mỗi Reset Token chỉ dùng được đúng 1 lần.
 */
@ApplicationScoped
public class ResetTokenBlacklist {

    private static final Logger LOG = Logger.getLogger(ResetTokenBlacklist.class);

    // jti → expiry epoch seconds (để cleanup)
    private final ConcurrentHashMap<String, Long> blacklist = new ConcurrentHashMap<>();

    public ResetTokenBlacklist() {
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "reset-token-blacklist-cleanup");
            t.setDaemon(true);
            return t;
        }).scheduleAtFixedRate(this::cleanup, 20, 20, TimeUnit.MINUTES);
    }

    /**
     * Vô hiệu hóa một Reset Token (sau khi đã dùng).
     *
     * @param jti           JWT ID của token
     * @param expiryEpochSec epoch second khi token hết hạn (để cleanup sau)
     */
    public void invalidate(String jti, long expiryEpochSec) {
        blacklist.put(jti, expiryEpochSec);
        LOG.debugf("Reset token blacklisted: jti=%s", jti);
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
            LOG.debugf("ResetTokenBlacklist cleanup: removed %d expired entries", removed);
        }
    }
}
