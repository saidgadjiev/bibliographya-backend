package ru.saidgadjiev.bibliographya.service.impl.verification;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Qualifier("hot")
public class HotInMemoryVerificationStorage implements VerificationStorage {

    private Cache<String, Map<String, Object>> hotCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    @Override
    public void removeAttr(HttpServletRequest request, String attr) {
        hotCache.getIfPresent(request.getRemoteAddr()).remove(attr);
    }

    @Override
    public Object getAttr(HttpServletRequest request, String attr) {
        Map<String, Object> values = hotCache.getIfPresent(request.getRemoteAddr());

        if (values == null) {
            return null;
        }

        return hotCache.getIfPresent(request.getRemoteAddr()).get(attr);
    }

    @Override
    public void setAttr(HttpServletRequest request, String attr, Object data) {
        Map<String, Object> values = hotCache.getIfPresent(request.getRemoteAddr());

        if (values == null) {
            hotCache.put(attr, new ConcurrentHashMap<>());
        }

        hotCache.getIfPresent(request.getRemoteAddr()).put(attr, data);
    }
    
    @Override
    public void expire(HttpServletRequest request) {
        hotCache.invalidate(request.getRemoteAddr());
    }
}
