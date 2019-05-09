package ru.saidgadjiev.bibliographya.security.cache;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.security.event.AddRoleEvent;
import ru.saidgadjiev.bibliographya.security.event.DeleteRoleEvent;
import ru.saidgadjiev.bibliographya.security.event.SignOutSuccessEvent;

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
    public void handle(DeleteRoleEvent deleteRoleEvent) {
        User user = userCache.getUserFromCache(deleteRoleEvent.getUserId());

        if (user != null) {
            user.getRoles().remove(deleteRoleEvent.getRole());
        }
    }

    @EventListener
    public void handle(AddRoleEvent addRoleEvent) {
        User user = userCache.getUserFromCache(addRoleEvent.getUserId());

        if (user != null) {
            user.getRoles().add(addRoleEvent.getRole());
        }
    }
}
