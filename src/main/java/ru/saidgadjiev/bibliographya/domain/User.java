package ru.saidgadjiev.bibliographya.domain;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;

import java.util.Collection;
import java.util.Set;

/**
 * Created by said on 22.10.2018.
 */
public class User implements UserDetails, CredentialsContainer {

    private int id;

    private ProviderType providerType;

    private UserAccount userAccount;

    private SocialAccount socialAccount;

    private Biography biography;

    private Set<Role> roles;

    private Boolean isNew = false;

    private boolean isDeleted = false;

    public User() {
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public String getPassword() {
        return userAccount == null ? null : userAccount.getPassword();
    }

    @Override
    public String getUsername() {
        return userAccount == null ? null : userAccount.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (userAccount != null) {
            return userAccount.isEmailVerified();
        }

        return true;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setName(String name) {
        if (userAccount != null) {
            userAccount.setEmail(name);
        }
    }

    public void setPassword(String password) {
        if (userAccount != null) {
            userAccount.setPassword(password);
        }
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Biography getBiography() {
        return biography;
    }

    public void setBiography(Biography biography) {
        this.biography = biography;
    }

    @Override
    public void eraseCredentials() {
        if (userAccount != null) {
            userAccount.setPassword(null);
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }

    public SocialAccount getSocialAccount() {
        return socialAccount;
    }

    public void setSocialAccount(SocialAccount socialAccount) {
        this.socialAccount = socialAccount;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean aNew) {
        isNew = aNew;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
