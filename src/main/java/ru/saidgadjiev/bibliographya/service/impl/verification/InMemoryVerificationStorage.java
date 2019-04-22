package ru.saidgadjiev.bibliographya.service.impl.verification;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;

@Service
public class InMemoryVerificationStorage implements VerificationStorage {

    private Cache<String, Map<String, Object>> cache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(30))
            .build();

    @Override
    public void removeAttr(HttpServletRequest request, String attr) {
        cache.getIfPresent(request.getRemoteAddr()).remove(attr);
    }

    @Override
    public Object getAttr(HttpServletRequest request, String attr) {
        return cache.getIfPresent(request.getRemoteAddr()).get(attr);
    }

    @Override
    public void setAttr(HttpServletRequest request, String attr, Object data) {
        cache.getIfPresent(request.getRemoteAddr()).put(attr, data);
    }
}
