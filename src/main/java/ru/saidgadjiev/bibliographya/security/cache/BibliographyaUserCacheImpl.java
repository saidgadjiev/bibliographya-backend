package ru.saidgadjiev.bibliographya.security.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.security.core.userdetails.UserDetails;
import ru.saidgadjiev.bibliographya.domain.User;

public class BibliographyaUserCacheImpl implements BibliographyaUserCache {

    private final org.springframework.cache.Cache cache;

    public BibliographyaUserCacheImpl(org.springframework.cache.Cache cache) {
        this.cache = cache;
    }

    @Override
    public User getUserFromCache(int id) {
        Cache<Integer, User> nativeCache = (Cache<Integer, User>) cache.getNativeCache();

        return nativeCache.getIfPresent(id);
    }

    @Override
    public void removeUserFromCache(int id) {
        cache.evict(id);
    }

    @Override
    public UserDetails getUserFromCache(String username) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putUserInCache(UserDetails user) {
        cache.put(((User) user).getId(), user);
    }

    @Override
    public void removeUserFromCache(String username) {
        throw new UnsupportedOperationException();
    }
}
