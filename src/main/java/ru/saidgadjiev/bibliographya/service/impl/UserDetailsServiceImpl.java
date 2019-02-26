package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import ru.saidgadjiev.bibliographya.model.RestorePassword;
import ru.saidgadjiev.bibliographya.model.SavePassword;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;

import javax.servlet.http.HttpServletRequest;
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

    private final SessionEmailVerificationService emailVerificationService;

    private final SecurityService securityService;

    @Autowired
    public UserDetailsServiceImpl(UserAccountDao userAccountDao,
                                  SocialAccountDao socialAccountDao,
                                  UserRoleDao userRoleDao,
                                  BiographyService biographyService,
                                  PasswordEncoder passwordEncoder,
                                  SessionEmailVerificationService emailVerificationService,
                                  SecurityService securityService) {
        this.userAccountDao = userAccountDao;
        this.socialAccountDao = socialAccountDao;
        this.userRoleDao = userRoleDao;
        this.biographyService = biographyService;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
        this.securityService = securityService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userAccountDao.getByEmail(email);

        if (user != null) {
            user.setRoles(userRoleDao.getRoles(user.getId()));
        }

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User save(SignUpRequest signUpRequest) throws SQLException {
        UserAccount userAccount = new UserAccount();

        userAccount.setEmail(signUpRequest.getEmail());
        userAccount.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        User user = new User();

        user.setProviderType(ProviderType.EMAIL_PASSWORD);
        user.setRoles(Stream.of(new Role(Role.ROLE_USER)).collect(Collectors.toSet()));
        user.setUserAccount(userAccount);
        user = userAccountDao.save(user);

        postSave(user, signUpRequest.getFirstName(), signUpRequest.getLastName(), signUpRequest.getMiddleName());

        return user;
    }

    @Override
    public User loadUserById(int userId) {
        User user = userAccountDao.getByUserId(userId);

        if (user != null) {
            user.setRoles(userRoleDao.getRoles(userId));
        }

        return user;
    }

    @Override
    public boolean isExistEmail(String email) {
        return userAccountDao.isExistEmail(email);
    }

    @Override
    public User loadSocialUserById(int userId) {
        User user = socialAccountDao.getByUserId(userId);

        if (user != null) {
            user.setRoles(userRoleDao.getRoles(userId));
        }

        return user;
    }

    @Override
    public User loadSocialUserByAccountId(ProviderType providerType, String accountId) {
        User user = socialAccountDao.getByAccountId(providerType, accountId);

        if (user != null) {
            user.setRoles(userRoleDao.getRoles(user.getId()));
        }

        return user;
    }

    @Override
    @Transactional
    public User saveSocialUser(SocialUserInfo userInfo) throws SQLException {
        SocialAccount socialAccount = new SocialAccount();

        socialAccount.setAccountId(userInfo.getId());

        User user = new User();

        user.setProviderType(ProviderType.fromId(userInfo.getProviderId()));
        user.setRoles(Stream.of(new Role(Role.ROLE_SOCIAL_USER)).collect(Collectors.toSet()));
        user.setSocialAccount(socialAccount);

        user = socialAccountDao.save(user);

        postSave(user, userInfo.getFirstName(), userInfo.getLastName(), userInfo.getMiddleName());

        return user;
    }

    @Override
    public HttpStatus savePassword(SavePassword savePassword) {
        User user = (User) securityService.findLoggedInUser();
        User actual = loadUserById(user.getId());

        if (passwordEncoder.matches(savePassword.getOldPassword(), actual.getPassword())) {
            userAccountDao.updatePassword(actual.getUsername(), savePassword.getNewPassword());

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public HttpStatus restorePassword(HttpServletRequest request, String email) {
        User actual = (User) loadUserByUsername(email);

        if (actual == null) {
            return HttpStatus.NOT_FOUND;
        }
        emailVerificationService.sendVerification(request, email);

        return HttpStatus.OK;
    }

    @Override
    public HttpStatus restorePassword(HttpServletRequest request, RestorePassword restorePassword) {
        EmailVerificationResult verificationResult = emailVerificationService.confirm(
                request,
                restorePassword.getEmail(),
                restorePassword.getCode()
        );

        if (verificationResult.isValid()) {
            int updated = userAccountDao.updatePassword(
                    restorePassword.getEmail(),
                    passwordEncoder.encode(restorePassword.getNewPassword())
            );

            if (updated == 0) {
                return HttpStatus.NOT_FOUND;
            }

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public HttpStatus saveEmail(HttpServletRequest request, SaveEmail saveEmail) {
        User actual = (User) securityService.findLoggedInUser();

        if (isExistEmail(saveEmail.getNewEmail())) {
            return HttpStatus.CONFLICT;
        }
        EmailVerificationResult emailVerificationResult = emailVerificationService.confirm(
                request,
                actual.getUsername(),
                saveEmail.getCode()
        );

        if (emailVerificationResult.isValid()) {
            int updated = userAccountDao.updateEmail(actual.getUsername(), saveEmail.getNewEmail());

            if (updated == 0) {
                return HttpStatus.NOT_FOUND;
            }

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    private void postSave(User user, String firstName, String lastName, String middleName) throws SQLException {
        userRoleDao.addRoles(user.getId(), user.getRoles());

        BiographyRequest biographyRequest = new BiographyRequest();

        biographyRequest.setFirstName(firstName);
        biographyRequest.setLastName(lastName);
        biographyRequest.setMiddleName(middleName);
        biographyRequest.setUserId(user.getId());

        Biography biography = biographyService.createAccountBiography(user, biographyRequest);

        user.setBiography(biography);
    }
}
