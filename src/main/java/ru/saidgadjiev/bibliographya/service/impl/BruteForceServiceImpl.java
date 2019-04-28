package ru.saidgadjiev.bibliographya.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by said on 27/04/2019.
 */
@Service
@Profile(BibliographyaConfiguration.PROFILE_PROD)
public class BruteForceServiceImpl implements ru.saidgadjiev.bibliographya.service.api.BruteForceService {

    private VerificationStorage verificationStorage;

    private Cache<String, AtomicLong> bruteCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(1))
            .build(s -> new AtomicLong());

    @Autowired
    public BruteForceServiceImpl(@Qualifier("inMemory") VerificationStorage verificationStorage) {
        this.verificationStorage = verificationStorage;
    }

    @Override
    public void count(HttpServletRequest request, Type type) {
        switch (type) {
            case SIGN_UP: {
                bruteCache.getIfPresent(getKey(request, Type.SIGN_UP)).incrementAndGet();
                bruteCache.invalidate(getKey(request, SessionState.SIGN_UP_CONFIRM, Type.SEND_VERIFICATION_CODE));
                break;
            }
            case SEND_VERIFICATION_CODE: {
                SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE);

                bruteCache.getIfPresent(getKey(request, sessionState, Type.SEND_VERIFICATION_CODE)).incrementAndGet();
                break;
            }
        }
    }

    @Override
    public boolean isBlocked(HttpServletRequest request, Type type) {
        switch (type) {
            case SIGN_UP:
                return bruteCache.getIfPresent(getKey(request, type)).get() >= type.getBlockedCount();
            case SEND_VERIFICATION_CODE:
                SessionState sessionState = (SessionState) verificationStorage.getAttr(request, VerificationStorage.STATE);

                return bruteCache.getIfPresent(getKey(request, sessionState, type)).get() >= type.getBlockedCount();
        }

        return false;
    }

    private String getKey(HttpServletRequest request, Type type) {
        return request.getRemoteAddr() + type.name();
    }

    private String getKey(HttpServletRequest request, SessionState sessionState, Type type) {
        return request.getRemoteAddr() + "_" + sessionState.name() + "_" + type.name();
    }

}
