package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliography.auth.common.ProviderType;
import ru.saidgadjiev.bibliography.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliography.dao.impl.SocialAccountDao;
import ru.saidgadjiev.bibliography.dao.impl.UserAccountDao;
import ru.saidgadjiev.bibliography.domain.*;
import ru.saidgadjiev.bibliography.model.BiographyRequest;
import ru.saidgadjiev.bibliography.model.SignUpRequest;
import ru.saidgadjiev.bibliography.service.api.UserAccountDetailsService;
import ru.saidgadjiev.bibliography.service.api.SocialUserDetailsService;

import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by said on 21.10.2018.
 */
@Service("userDetailsService")
public class UserDetailsAccountDetailsServiceImpl implements UserDetailsService, UserAccountDetailsService, SocialUserDetailsService {

    private final UserAccountDao userAccountDao;

    private final SocialAccountDao socialAccountDao;

    private final BiographyService biographyService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDetailsAccountDetailsServiceImpl(UserAccountDao userAccountDao,
                                                SocialAccountDao socialAccountDao,
                                                BiographyService biographyService,
                                                PasswordEncoder passwordEncoder) {
        this.userAccountDao = userAccountDao;
        this.socialAccountDao = socialAccountDao;
        this.biographyService = biographyService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAccountDao.getByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User save(SignUpRequest signUpRequest) throws SQLException {
        UserAccount userAccount = new UserAccount();

        userAccount.setName(signUpRequest.getUsername());
        userAccount.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        User user = new User();

        user.setRoles(Stream.of(new Role(Role.ROLE_USER)).collect(Collectors.toSet()));

        user.setUserAccount(userAccount);

        user = userAccountDao.save(user);
        BiographyRequest biographyRequest = new BiographyRequest();

        biographyRequest.setFirstName(signUpRequest.getFirstName());
        biographyRequest.setLastName(signUpRequest.getLastName());
        biographyRequest.setMiddleName(signUpRequest.getMiddleName());
        biographyRequest.setUserId(user.getId());

        Biography biography = biographyService.createAccountBiography(user, biographyRequest);

        user.setBiography(biography);

        return user;
    }

    @Override
    public User loadUserById(int userId) {
        return userAccountDao.getById(userId);
    }

    @Override
    public boolean isExistUserName(String username) {
        return userAccountDao.isExistUsername(username);
    }

    @Override
    public UserDetails loadSocialUserById(int userId) {
        return socialAccountDao.getByUserId(userId);
    }

    @Override
    public UserDetails loadSocialUserByAccountId(ProviderType providerType, String accountId) {
        return socialAccountDao.getByAccountId(providerType, accountId);
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

        user = socialAccountDao.save(user);
        BiographyRequest biographyRequest = new BiographyRequest();

        biographyRequest.setFirstName(userInfo.getFirstName());
        biographyRequest.setLastName(userInfo.getLastName());
        biographyRequest.setMiddleName(userInfo.getMiddleName());
        biographyRequest.setUserId(user.getId());

        Biography biography = biographyService.createAccountBiography(user, biographyRequest);

        user.setBiography(biography);

        return user;
    }
}
