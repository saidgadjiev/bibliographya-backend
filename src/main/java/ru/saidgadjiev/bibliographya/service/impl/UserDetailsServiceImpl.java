package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.dao.impl.SocialAccountDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserAccountDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserRoleDao;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.BiographyRequest;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;

import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by said on 21.10.2018.
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService, BibliographyaUserDetailsService {

    private final UserAccountDao userAccountDao;

    private final SocialAccountDao socialAccountDao;

    private final UserRoleDao userRoleDao;

    private BiographyService biographyService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDetailsServiceImpl(UserAccountDao userAccountDao,
                                  SocialAccountDao socialAccountDao,
                                  UserRoleDao userRoleDao,
                                  PasswordEncoder passwordEncoder) {
        this.userAccountDao = userAccountDao;
        this.socialAccountDao = socialAccountDao;
        this.userRoleDao = userRoleDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userAccountDao.getByUsername(username);

        user.setRoles(userRoleDao.getRoles(user.getId()));

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User save(SignUpRequest signUpRequest) throws SQLException {
        UserAccount userAccount = new UserAccount();

        userAccount.setName(signUpRequest.getUsername());
        userAccount.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        User user = new User();

        user.setProviderType(ProviderType.USERNAME_PASSWORD);
        user.setRoles(Stream.of(new Role(Role.ROLE_USER)).collect(Collectors.toSet()));
        user.setUserAccount(userAccount);

        postSave(user, signUpRequest.getFirstName(), signUpRequest.getLastName(), signUpRequest.getMiddleName());

        return user;
    }

    @Override
    public User loadUserById(int userId) {
        User user = userAccountDao.getByUserId(userId);

        user.setRoles(userRoleDao.getRoles(userId));

        return user;
    }

    @Override
    public boolean isExistUserName(String username) {
        return userAccountDao.isExistUsername(username);
    }

    @Override
    public UserDetails loadSocialUserById(int userId) {
        User user = socialAccountDao.getByUserId(userId);

        user.setRoles(userRoleDao.getRoles(userId));

        return user;
    }

    @Override
    public UserDetails loadSocialUserByAccountId(ProviderType providerType, String accountId) {
        User user = socialAccountDao.getByAccountId(providerType, accountId);

        user.setRoles(userRoleDao.getRoles(user.getId()));

        return user;
    }

    @Override
    @Transactional
    public UserDetails saveSocialUser(SocialUserInfo userInfo) throws SQLException {
        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setAccountId(userInfo.getId());

        User user = new User();

        user.setProviderType(ProviderType.fromId(userInfo.getProviderId()));
        user.setRoles(Stream.of(new Role(Role.ROLE_SOCIAL_USER)).collect(Collectors.toSet()));
        user.setSocialAccount(socialAccount);

        postSave(user, userInfo.getFirstName(), userInfo.getLastName(), userInfo.getMiddleName());

        return user;
    }

    @Autowired
    public void setBiographyService(BiographyService biographyService) {
        this.biographyService = biographyService;
    }

    private void postSave(User user, String firtName, String lastName, String middleName) throws SQLException {
        user = userAccountDao.save(user);
        userRoleDao.addRoles(user.getId(), user.getRoles());

        BiographyRequest biographyRequest = new BiographyRequest();

        biographyRequest.setFirstName(firtName);
        biographyRequest.setLastName(lastName);
        biographyRequest.setMiddleName(middleName);
        biographyRequest.setUserId(user.getId());

        Biography biography = biographyService.createAccountBiography(user, biographyRequest);

        user.setBiography(biography);
    }
}
