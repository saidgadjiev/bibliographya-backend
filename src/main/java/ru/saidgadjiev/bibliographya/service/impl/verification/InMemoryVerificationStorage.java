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

/**
 * Created by said on 23/04/2019.
 */
@Service
@Qualifier("inMemory")
public class InMemoryVerificationStorage implements VerificationStorage {

    private Cache<String, Map<String, Object>> coldCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    @Override
    public void removeAttr(HttpServletRequest request, String attr) {
        Map<String, Object> cache = coldCache.getIfPresent(request.getRemoteAddr());

        if (cache == null) {
            return;
        }

        cache.remove(attr);
    }

    @Override
    public Object getAttr(HttpServletRequest request, String attr) {
        Map<String, Object> values = coldCache.getIfPresent(request.getRemoteAddr());

        if (values == null) {
            return null;
        }

        return values.get(attr);
    }

    @Override
    public Object getAttr(HttpServletRequest request, String attr, Object defaultValue) {
        Object attrValue = getAttr(request, attr);

        return attrValue == null ? defaultValue : attrValue;
    }

    @Override
    public void setAttr(HttpServletRequest request, String attr, Object data) {
        Map<String, Object> values = coldCache.getIfPresent(request.getRemoteAddr());

        if (values == null) {
            coldCache.put(request.getRemoteAddr(), new ConcurrentHashMap<>());
        }

        coldCache.getIfPresent(request.getRemoteAddr()).put(attr, data);
    }

    @Override
    public void expire(HttpServletRequest request) {
        coldCache.invalidate(request.getRemoteAddr());
    }
}
