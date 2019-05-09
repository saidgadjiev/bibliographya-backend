package ru.saidgadjiev.bibliographya.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang.StringUtils;
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

    public static final String ID = "id";

    public static final String TABLE = "user";

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

    @JsonIgnore
    public String getPassword() {
        switch (providerType) {
            case SIMPLE:
                return userAccount.getPassword();
            default:
                throw new UnsupportedOperationException();
        }
    }

    @Override
    @JsonIgnore
    public String getUsername() {
        switch (providerType) {
            case FACEBOOK:
            case VK:
                return providerType.getId() + "_" + socialAccount.getAccountId();
            case SIMPLE:
                if (StringUtils.isBlank(userAccount.getPhone())) {
                    return userAccount.getEmail();
                }

                return userAccount.getPhone();
        }

        throw new UnsupportedOperationException();
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
        switch (providerType) {
            case SIMPLE:
                userAccount.setPassword(null);
                break;
        }
    }

    public Boolean getNew() {
        return isNew;
    }

    public void setNew(Boolean aNew) {
        isNew = aNew;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public ProviderType getProviderType() {
        return providerType;
    }

    public void setProviderType(ProviderType providerType) {
        this.providerType = providerType;
    }

    public SocialAccount getSocialAccount() {
        return socialAccount;
    }

    public void setSocialAccount(SocialAccount socialAccount) {
        this.socialAccount = socialAccount;
    }

    public UserAccount getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(UserAccount userAccount) {
        this.userAccount = userAccount;
    }
}
