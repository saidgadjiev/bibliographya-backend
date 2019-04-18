package ru.saidgadjiev.bibliographya.service.impl.counter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.service.impl.SecurityService;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;

@Service
public class ViewCounterService {

    private Cache<String, Boolean> viewedCache = Caffeine.newBuilder()
            .maximumSize(100)
            .expireAfterWrite(Duration.ofMinutes(10))
            .build();

    private SecurityService securityService;

    private ViewCounter viewCounter;

    @Autowired
    public ViewCounterService(SecurityService securityService, ViewCounter viewCounter) {
        this.securityService = securityService;
        this.viewCounter = viewCounter;
    }

    @Async
    public void hit(HttpServletRequest request, int biographyId) {
        if (viewedCache.getIfPresent(getKey(request, biographyId)) != null) {
            viewCounter.hit(biographyId);
        }
    }

    private String getKey(HttpServletRequest request, int biographyId) {
        User user = (User) securityService.findLoggedInUser();

        if (user != null) {
            return user.getId() + "_" + biographyId;
        }

        return request.getRemoteAddr() + "_" + biographyId;
    }
}
