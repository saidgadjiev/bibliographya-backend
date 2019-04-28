package ru.saidgadjiev.bibliographya.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by said on 27/04/2019.
 */
@Service
public class BruteForceService {

    private Cache<String, AtomicLong> bruteCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(1))
            .build(s -> new AtomicLong());

    public void count(HttpServletRequest request, Type type) {
        bruteCache.getIfPresent(getKey(request, type)).incrementAndGet();
    }

    public void expire(HttpServletRequest request, Type type) {
        bruteCache.invalidate(getKey(request, type));
    }

    public boolean isBlocked(HttpServletRequest request, Type type) {
        return bruteCache.getIfPresent(getKey(request, type)).get() >= type.blockedCount;
    }

    private String getKey(HttpServletRequest request, Type type) {
        return request.getRemoteAddr() + type.name();
    }

    public enum Type {

        SIGN_UP(3),

        SEND_VERIFICATION_CODE(3);

        private int blockedCount;

        Type(int blockedCount) {
            this.blockedCount = blockedCount;
        }
    }
}
