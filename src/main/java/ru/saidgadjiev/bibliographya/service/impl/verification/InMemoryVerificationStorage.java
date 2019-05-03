package ru.saidgadjiev.bibliographya.service.impl.verification;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
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
@Service("inMemory")
public class InMemoryVerificationStorage implements VerificationStorage {

    private LoadingCache<String, Map<String, Object>> coldCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .build(key -> new ConcurrentHashMap<>());

    @Override
    public void removeAttr(HttpServletRequest request, String attr) {
        coldCache.get(request.getRemoteAddr()).remove(attr);
    }

    @Override
    public Object getAttr(HttpServletRequest request, String attr) {
        return coldCache.get(request.getRemoteAddr()).get(attr);
    }

    @Override
    public Object getAttr(HttpServletRequest request, String attr, Object defaultValue) {
        Object attrValue = getAttr(request, attr);

        return attrValue == null ? defaultValue : attrValue;
    }

    @Override
    public void setAttr(HttpServletRequest request, String attr, Object data) {
        coldCache.get(request.getRemoteAddr()).put(attr, data);
    }

    @Override
    public void expire(HttpServletRequest request) {
        coldCache.invalidate(request.getRemoteAddr());
    }
}
