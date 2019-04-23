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
@Qualifier("cold")
public class ColdInMemoryVerificationStorage implements VerificationStorage {

    private Cache<String, Map<String, Object>> coldCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    @Override
    public void removeAttr(HttpServletRequest request, String attr) {
        coldCache.getIfPresent(request.getRemoteAddr()).remove(attr);
    }

    @Override
    public Object getAttr(HttpServletRequest request, String attr) {
        Map<String, Object> values = coldCache.getIfPresent(request.getRemoteAddr());

        if (values == null) {
            return null;
        }

        return coldCache.getIfPresent(request.getRemoteAddr()).get(attr);
    }

    @Override
    public void setAttr(HttpServletRequest request, String attr, Object data) {
        Map<String, Object> values = coldCache.getIfPresent(request.getRemoteAddr());

        if (values == null) {
            coldCache.put(attr, new ConcurrentHashMap<>());
        }

        coldCache.getIfPresent(request.getRemoteAddr()).put(attr, data);
    }
}
