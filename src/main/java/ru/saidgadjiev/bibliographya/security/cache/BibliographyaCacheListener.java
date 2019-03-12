package ru.saidgadjiev.bibliographya.security.cache;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.security.event.SignOutSuccessEvent;
import ru.saidgadjiev.bibliographya.security.event.UnverifyEmailsEvent;
import ru.saidgadjiev.bibliographya.security.event.ChangeEmailEvent;

import java.util.Collection;

@Component
public class BibliographyaCacheListener {

    private BibliographyaUserCache userCache;

    public BibliographyaCacheListener(BibliographyaUserCache userCache) {
        this.userCache = userCache;
    }

    @EventListener
    public void handle(AuthenticationSuccessEvent authenticationSuccessEvent) {
        Authentication authentication = authenticationSuccessEvent.getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        userCache.putUserInCache(userDetails);
    }

    @EventListener
    public void handle(SignOutSuccessEvent signOutSuccessEvent) {
        Authentication authentication = signOutSuccessEvent.getAuthentication();
        User userDetails = (User) authentication.getPrincipal();

        userCache.removeUserFromCache(userDetails.getId());
    }

    @EventListener
    public void handle(UnverifyEmailsEvent unverifyEmailsEvent) {
        Collection<User> usersFromCache = userCache.getUsersFromCache(unverifyEmailsEvent.getEmail());

        usersFromCache.forEach(user -> user.setEmailVerified(false));
    }

    @EventListener
    public void handle(ChangeEmailEvent changeEmailEvent) {
        User user = userCache.getUserFromCache(changeEmailEvent.getUser().getId());

        user.setEmailVerified(true);
        user.setEmail(changeEmailEvent.getUser().getEmail());
    }
}
