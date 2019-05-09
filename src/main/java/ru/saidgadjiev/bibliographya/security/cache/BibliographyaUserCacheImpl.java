package ru.saidgadjiev.bibliographya.security.cache;

import org.springframework.cache.Cache;
import org.springframework.security.core.userdetails.UserDetails;
import ru.saidgadjiev.bibliographya.domain.User;

import java.util.Map;

public class BibliographyaUserCacheImpl implements BibliographyaUserCache {

    private final Cache cache;

    public BibliographyaUserCacheImpl(Cache cache) {
        this.cache = cache;
    }

    @Override
    public User getUserFromCache(int id) {
        com.github.benmanes.caffeine.cache.Cache<String, User> nativeCache = (com.github.benmanes.caffeine.cache.Cache<String, User>) cache.getNativeCache();

        Map<String, User> userMap = nativeCache.asMap();

        return userMap.values().stream().filter(user -> user.getId() == id).findAny().orElse(null);
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
