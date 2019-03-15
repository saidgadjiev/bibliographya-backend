package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserRoleDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.BiographyRequest;
import ru.saidgadjiev.bibliographya.model.RestorePassword;
import ru.saidgadjiev.bibliographya.model.SavePassword;
import ru.saidgadjiev.bibliographya.security.event.UnverifyEmailsEvent;
import ru.saidgadjiev.bibliographya.security.event.ChangeEmailEvent;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;

import javax.mail.MessagingException;
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

    private final UserDao userDao;

    private final GeneralDao generalDao;

    private final UserRoleDao userRoleDao;

    private BiographyService biographyService;

    private final PasswordEncoder passwordEncoder;

    private final HttpSessionEmailVerificationService emailVerificationService;

    private final SecurityService securityService;

    private final HttpSessionManager httpSessionManager;

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserDetailsServiceImpl(UserDao userDao,
                                  GeneralDao generalDao,
                                  UserRoleDao userRoleDao,
                                  BiographyService biographyService,
                                  PasswordEncoder passwordEncoder,
                                  HttpSessionEmailVerificationService emailVerificationService,
                                  SecurityService securityService,
                                  HttpSessionManager httpSessionManager,
                                  ApplicationEventPublisher eventPublisher) {
        this.userDao = userDao;
        this.generalDao = generalDao;
        this.userRoleDao = userRoleDao;
        this.biographyService = biographyService;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
        this.securityService = securityService;
        this.httpSessionManager = httpSessionManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Collection<FilterCriteria> userCriteria = new ArrayList<>();

        userCriteria.add(
                new FilterCriteria.Builder<String>()
                        .propertyName(User.EMAIL)
                        .valueSetter(PreparedStatement::setString)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(email)
                        .build()

        );
        userCriteria.add(
                new FilterCriteria.Builder<Boolean>()
                        .propertyName(User.EMAIL_VERIFIED)
                        .valueSetter(PreparedStatement::setBoolean)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(true)
                        .build()

        );

        User user = userDao.get(userCriteria);

        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        user.setRoles(userRoleDao.getRoles(user.getId()));

        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User save(User saveUser) throws SQLException {
        Biography saveBiography = saveUser.getBiography();
        unverifyEmails(saveUser.getEmail());

        User user = new User();

        user.setEmail(saveUser.getEmail());
        user.setEmailVerified(true);
        user.setPassword(passwordEncoder.encode(saveUser.getPassword()));

        user.setRoles(Stream.of(new Role(Role.ROLE_USER)).collect(Collectors.toSet()));
        user = userDao.save(user);

        postSave(user, saveBiography.getFirstName(), saveBiography.getLastName(), saveBiography.getMiddleName());

        return user;
    }

    @Override
    public User loadUserById(int id) {
        Collection<FilterCriteria> userCriteria = new ArrayList<>();

        userCriteria.add(
                new FilterCriteria.Builder<Integer>()
                        .propertyName(User.ID)
                        .valueSetter(PreparedStatement::setInt)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(id)
                        .build()
        );

        User user = userDao.get(userCriteria);

        if (user != null) {
            user.setRoles(userRoleDao.getRoles(user.getId()));
        }

        return user;
    }

    @Override
    public boolean isExistEmail(String email) {
        return userDao.isExistEmail(email);
    }

    @Override
    public HttpStatus savePassword(SavePassword savePassword) {
        User user = (User) securityService.findLoggedInUser();

        List<Map<String, Object>> fieldsValues = generalDao.getFields(
                User.TABLE,
                Collections.singletonList(User.PASSWORD),
                Collections.singletonList(
                        new FilterCriteria.Builder<Integer>()
                                .propertyName(User.ID)
                                .filterOperation(FilterOperation.EQ)
                                .filterValue(user.getId())
                                .needPreparedSet(true)
                                .valueSetter(PreparedStatement::setInt)
                                .build()
                )
        );
        Map<String, Object> row = fieldsValues.get(0);
        String password = (String) row.get(User.PASSWORD);

        if (passwordEncoder.matches(savePassword.getOldPassword(), password)) {
            List<UpdateValue> values = new ArrayList<>();

            values.add(
                    new UpdateValue<>(
                            User.PASSWORD,
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
                            .propertyName(User.ID)
                            .valueSetter(PreparedStatement::setInt)
                            .build()
            );

            generalDao.update(User.TABLE, values, criteria);

            return HttpStatus.OK;
        }

        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public HttpStatus restorePasswordStart(HttpServletRequest request, Locale locale, String email) throws MessagingException {
        Collection<FilterCriteria> userCriteria = new ArrayList<>();

        userCriteria.add(
                new FilterCriteria.Builder<String>()
                        .propertyName(User.EMAIL)
                        .valueSetter(PreparedStatement::setString)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(email)
                        .build()

        );
        userCriteria.add(
                new FilterCriteria.Builder<Boolean>()
                        .propertyName(User.EMAIL_VERIFIED)
                        .valueSetter(PreparedStatement::setBoolean)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(true)
                        .build()

        );

        User actual = userDao.get(userCriteria);

        if (actual == null) {
            return HttpStatus.NOT_FOUND;
        }
        httpSessionManager.setRestorePassword(request, actual);
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
                            User.PASSWORD,
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
                            .propertyName(User.EMAIL)
                            .valueSetter(PreparedStatement::setString)
                            .build()
            );
            criteria.add(
                    new FilterCriteria.Builder<Boolean>()
                            .filterOperation(FilterOperation.EQ)
                            .filterValue(true)
                            .needPreparedSet(true)
                            .propertyName(User.EMAIL_VERIFIED)
                            .valueSetter(PreparedStatement::setBoolean)
                            .build()
            );

            int updated = generalDao.update(User.TABLE, values, criteria);

            if (updated == 0) {
                return HttpStatus.NOT_FOUND;
            }

            httpSessionManager.removeState(request);

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public HttpStatus saveEmailFinish(HttpServletRequest request, EmailConfirmation emailConfirmation) {
        User actual = (User) securityService.findLoggedInUser();

        EmailVerificationResult emailVerificationResult = emailVerificationService.verify(
                request,
                emailConfirmation.getEmail(),
                emailConfirmation.getCode()
        );

        if (emailVerificationResult.isValid()) {
            //1. Отвязываем почту у всех людей
            unverifyEmails(emailConfirmation.getEmail());

            //1. Обновление email с привязкой данного пользователя
            List<UpdateValue> values = new ArrayList<>();

            values.add(
                    new UpdateValue<>(
                            User.EMAIL,
                            emailConfirmation.getEmail(),
                            PreparedStatement::setString
                    )
            );

            values.add(
                    new UpdateValue<>(
                            User.EMAIL_VERIFIED,
                            true,
                            PreparedStatement::setBoolean
                    )
            );

            List<FilterCriteria> criteria = new ArrayList<>();

            criteria.add(
                    new FilterCriteria.Builder<Integer>()
                            .filterOperation(FilterOperation.EQ)
                            .filterValue(actual.getId())
                            .needPreparedSet(true)
                            .propertyName(User.ID)
                            .valueSetter(PreparedStatement::setInt)
                            .build()
            );

            generalDao.update(User.TABLE, values, criteria);

            httpSessionManager.removeState(request);

            actual.setEmail(emailConfirmation.getEmail());
            actual.setEmailVerified(true);

            eventPublisher.publishEvent(new ChangeEmailEvent(actual));

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public HttpStatus saveEmailStart(HttpServletRequest request, Locale locale, String email) throws MessagingException {
        User user = (User) securityService.findLoggedInUser();

        httpSessionManager.setChangeEmail(request, email, user);

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
                        User.EMAIL_VERIFIED,
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
                        .propertyName(User.EMAIL)
                        .valueSetter(PreparedStatement::setString)
                        .build()
        );

        generalDao.update(User.TABLE, valuesRemoveEmailsVerification, criteriaRemoveEmailsVerification);

        eventPublisher.publishEvent(new UnverifyEmailsEvent(email));
    }
}
