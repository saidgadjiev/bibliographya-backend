package ru.saidgadjiev.bibliography.domain;

import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.saidgadjiev.bibliography.auth.ProviderType;

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

    public User(int id,
                ProviderType providerType,
                UserAccount userAccount,
                SocialAccount socialAccount,
                Biography biography,
                Set<Role> roles) {
        this.id = id;
        this.providerType = providerType;
        this.userAccount = userAccount;
        this.socialAccount = socialAccount;
        this.biography = biography;
        this.roles = roles;
    }

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
        return userAccount == null ? null : userAccount.getName();
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
        return true;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setName(String name) {
        if (userAccount != null) {
            userAccount.setName(name);
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
}
