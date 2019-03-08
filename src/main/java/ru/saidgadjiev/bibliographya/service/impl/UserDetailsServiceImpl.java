package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.dao.impl.SocialAccountDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserAccountDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserRoleDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.BiographyRequest;
import ru.saidgadjiev.bibliographya.model.RestorePassword;
import ru.saidgadjiev.bibliographya.model.SavePassword;
import ru.saidgadjiev.bibliographya.model.SignUpRequest;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;

import javax.servlet.http.HttpServletRequest;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by said on 21.10.2018.
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements BibliographyaUserDetailsService {

    private final UserAccountDao userAccountDao;

    private final SocialAccountDao socialAccountDao;

    private final GeneralDao generalDao;

    private final UserRoleDao userRoleDao;

    private BiographyService biographyService;

    private final PasswordEncoder passwordEncoder;

    private final SessionEmailVerificationService emailVerificationService;

    private final SecurityService securityService;

    private final SessionManager sessionManager;

    @Autowired
    public UserDetailsServiceImpl(UserAccountDao userAccountDao,
                                  SocialAccountDao socialAccountDao,
                                  GeneralDao generalDao,
                                  UserRoleDao userRoleDao,
                                  BiographyService biographyService,
                                  PasswordEncoder passwordEncoder,
                                  SessionEmailVerificationService emailVerificationService,
                                  SecurityService securityService,
                                  SessionManager sessionManager) {
        this.userAccountDao = userAccountDao;
        this.socialAccountDao = socialAccountDao;
        this.generalDao = generalDao;
        this.userRoleDao = userRoleDao;
        this.biographyService = biographyService;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
        this.securityService = securityService;
        this.sessionManager = sessionManager;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userAccountDao.getByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        user.setRoles(userRoleDao.getRoles(user.getId()));

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User save(SignUpRequest signUpRequest) throws SQLException {
        unverifyEmails(signUpRequest.getEmail());

        UserAccount userAccount = new UserAccount();

        userAccount.setEmail(signUpRequest.getEmail());
        userAccount.setEmailVerified(true);
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
    public User loadUserAccountById(int id) {
        User user = userAccountDao.getById(id);

        if (user != null) {
            user.setRoles(userRoleDao.getRoles(user.getId()));
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

        if (!user.getProviderType().equals(ProviderType.EMAIL_PASSWORD)) {
            return HttpStatus.BAD_REQUEST;
        }
        List<Map<String, Object>> fieldsValues = generalDao.getFields(
                UserAccount.TABLE,
                Collections.singletonList(UserAccount.PASSWORD),
                Collections.singletonList(
                        new FilterCriteria.Builder<Integer>()
                                .propertyName(UserAccount.ID)
                                .filterOperation(FilterOperation.EQ)
                                .filterValue(user.getUserAccount().getId())
                                .needPreparedSet(true)
                                .valueSetter(PreparedStatement::setInt)
                                .build()
                )
        );
        Map<String, Object> row = fieldsValues.get(0);
        String password = (String) row.get(UserAccount.PASSWORD);

        if (passwordEncoder.matches(savePassword.getOldPassword(), password)) {
            List<UpdateValue> values = new ArrayList<>();

            values.add(
                    new UpdateValue<>(
                            UserAccount.PASSWORD,
                            passwordEncoder.encode(savePassword.getNewPassword()),
                            PreparedStatement::setString
                    )
            );
            List<FilterCriteria> criteria = new ArrayList<>();

            criteria.add(
                    new FilterCriteria.Builder<Integer>()
                            .filterOperation(FilterOperation.EQ)
                            .filterValue(user.getId())
                            .needPreparedSet(true)
                            .propertyName(UserAccount.ID)
                            .valueSetter(PreparedStatement::setInt)
                            .build()
            );

            userAccountDao.update(values, criteria);

            return HttpStatus.OK;
        }

        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public HttpStatus restorePasswordStart(HttpServletRequest request, Locale locale, String email) {
        User actual = userAccountDao.getByEmail(email);

        if (actual == null) {
            return HttpStatus.NOT_FOUND;
        }
        sessionManager.setRestorePassword(request, actual);
        emailVerificationService.sendVerification(request, locale, email);

        return HttpStatus.OK;
    }

    @Override
    public HttpStatus restorePasswordFinish(HttpServletRequest request, RestorePassword restorePassword) {
        EmailVerificationResult verificationResult = emailVerificationService.verify(
                request,
                restorePassword.getEmail(),
                restorePassword.getCode()
        );

        if (verificationResult.isValid()) {
            List<UpdateValue> values = new ArrayList<>();

            values.add(
                    new UpdateValue<>(
                            UserAccount.PASSWORD,
                            passwordEncoder.encode(restorePassword.getPassword()),
                            PreparedStatement::setString
                    )
            );
            List<FilterCriteria> criteria = new ArrayList<>();

            criteria.add(
                    new FilterCriteria.Builder<String>()
                            .filterOperation(FilterOperation.EQ)
                            .filterValue(restorePassword.getEmail())
                            .needPreparedSet(true)
                            .propertyName(UserAccount.EMAIL)
                            .valueSetter(PreparedStatement::setString)
                            .build()
            );
            criteria.add(
                    new FilterCriteria.Builder<Boolean>()
                            .filterOperation(FilterOperation.EQ)
                            .filterValue(true)
                            .needPreparedSet(true)
                            .propertyName(UserAccount.EMAIL_VERIFIED)
                            .valueSetter(PreparedStatement::setBoolean)
                            .build()
            );

            int updated = userAccountDao.update(values, criteria);

            if (updated == 0) {
                return HttpStatus.NOT_FOUND;
            }

            sessionManager.removeState(request);

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public HttpStatus saveEmailFinish(HttpServletRequest request, SaveEmail saveEmail) {
        User actual = (User) securityService.findLoggedInUser();

        EmailVerificationResult emailVerificationResult = emailVerificationService.verify(
                request,
                saveEmail.getEmail(),
                saveEmail.getCode()
        );

        if (emailVerificationResult.isValid()) {
            //1. Отвязываем почту у всех людей
            unverifyEmails(saveEmail.getEmail());

            //1. Обновление email с привязкой данного пользователя
            List<UpdateValue> values = new ArrayList<>();

            values.add(
                    new UpdateValue<>(
                            UserAccount.EMAIL,
                            saveEmail.getEmail(),
                            PreparedStatement::setString
                    )
            );

            values.add(
                    new UpdateValue<>(
                            UserAccount.EMAIL_VERIFIED,
                            true,
                            PreparedStatement::setBoolean
                    )
            );

            List<FilterCriteria> criteria = new ArrayList<>();

            criteria.add(
                    new FilterCriteria.Builder<Integer>()
                            .filterOperation(FilterOperation.EQ)
                            .filterValue(actual.getUserAccount().getId())
                            .needPreparedSet(true)
                            .propertyName(UserAccount.ID)
                            .valueSetter(PreparedStatement::setInt)
                            .build()
            );

            userAccountDao.update(values, criteria);

            sessionManager.removeState(request);

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public HttpStatus saveEmailStart(HttpServletRequest request, Locale locale, String email) {
        User user = (User) securityService.findLoggedInUser();

        sessionManager.setChangeEmail(request, email, user);

        emailVerificationService.sendVerification(request, locale, email);

        return HttpStatus.OK;
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

    private void unverifyEmails(String email) {
        List<UpdateValue> valuesRemoveEmailsVerification = new ArrayList<>();

        valuesRemoveEmailsVerification.add(
                new UpdateValue<>(
                        UserAccount.EMAIL_VERIFIED,
                        false,
                        PreparedStatement::setBoolean
                )
        );

        List<FilterCriteria> criteriaRemoveEmailsVerification = new ArrayList<>();

        criteriaRemoveEmailsVerification.add(
                new FilterCriteria.Builder<String>()
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(email)
                        .needPreparedSet(true)
                        .propertyName(UserAccount.EMAIL)
                        .valueSetter(PreparedStatement::setString)
                        .build()
        );

        userAccountDao.update(valuesRemoveEmailsVerification, criteriaRemoveEmailsVerification);
    }
}
