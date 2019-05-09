package ru.saidgadjiev.bibliographya.security.cache;

import org.springframework.security.core.userdetails.UserCache;
import ru.saidgadjiev.bibliographya.domain.User;

import java.util.Collection;

public interface BibliographyaUserCache extends UserCache {

    User getUserFromCache(int id);

    void removeUserFromCache(int id);

}
