package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserDao;
import ru.saidgadjiev.bibliographya.dao.impl.UserRoleDao;
import ru.saidgadjiev.bibliographya.data.AuthKeyArgumentResolver;
import ru.saidgadjiev.bibliographya.data.PreparedSetter;
import ru.saidgadjiev.bibliographya.data.UpdateValue;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.BiographyRequest;
import ru.saidgadjiev.bibliographya.model.RestorePassword;
import ru.saidgadjiev.bibliographya.model.SavePassword;
import ru.saidgadjiev.bibliographya.model.SessionState;
import ru.saidgadjiev.bibliographya.security.event.ChangeEmailEvent;
import ru.saidgadjiev.bibliographya.security.event.ChangePhoneEvent;
import ru.saidgadjiev.bibliographya.security.event.UnverifyEmailsEvent;
import ru.saidgadjiev.bibliographya.security.event.UnverifyPhonesEvent;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.api.VerificationService;
import ru.saidgadjiev.bibliographya.service.api.VerificationStorage;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.sql.Types;
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

    private final VerificationService verificationService;

    private final SecurityService securityService;

    private final VerificationStorage verificationStorage;

    private ApplicationEventPublisher eventPublisher;

    @Autowired
    public UserDetailsServiceImpl(UserDao userDao,
                                  GeneralDao generalDao,
                                  UserRoleDao userRoleDao,
                                  BiographyService biographyService,
                                  PasswordEncoder passwordEncoder,
                                  @Qualifier("wrapper") VerificationService verificationService,
                                  SecurityService securityService,
                                  @Qualifier("inMemory") VerificationStorage verificationStorage,
                                  ApplicationEventPublisher eventPublisher) {
        this.userDao = userDao;
        this.generalDao = generalDao;
        this.userRoleDao = userRoleDao;
        this.biographyService = biographyService;
        this.passwordEncoder = passwordEncoder;
        this.verificationService = verificationService;
        this.securityService = securityService;
        this.verificationStorage = verificationStorage;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public User loadUserByUsername(AuthKey authKey) throws UsernameNotFoundException {
        User user = null;

        switch (authKey.getType()) {
            case PHONE: {
                user = userDao.getByPhone(authKey.formattedNumber());
                break;
            }
            case EMAIL: {
                user = userDao.getByPhone(authKey.getEmail());
                break;
            }
        }

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

        user.setPhone(saveUser.getPhone());
        user.setPassword(passwordEncoder.encode(saveUser.getPassword()));

        user.setRoles(Stream.of(new Role(Role.ROLE_USER)).collect(Collectors.toSet()));
        user = userDao.save(user);

        postSave(user, saveBiography.getFirstName(), saveBiography.getLastName(), saveBiography.getMiddleName());

        return user;
    }

    @Override
    public User loadUserById(int id) {
        User user = userDao.getUniqueUser(new AndCondition() {{
            add(new Equals(new ColumnSpec(User.ID), new Param()));
        }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, id)));

        if (user != null) {
            user.setRoles(userRoleDao.getRoles(user.getId()));
        }

        return user;
    }

    @Override
    public boolean isExist(AuthKey authKey) {
        switch (authKey.getType()) {
            case PHONE:
                return userDao.isExistPhone(authKey.formattedNumber());
            case EMAIL:
                return userDao.isExistEmail(authKey.getEmail());
        }

        return false;
    }

    @Override
    public HttpStatus savePassword(SavePassword savePassword) {
        User user = (User) securityService.findLoggedInUser();

        List<Map<String, Object>> fieldsValues = generalDao.getFields(
                User.TABLE,
                Collections.singletonList(User.PASSWORD),
                new AndCondition() {{
                    new Equals(new ColumnSpec(User.ID), new Param());
                }},
                Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, user.getId()))
        );
        Map<String, Object> row = fieldsValues.get(0);
        String password = (String) row.get(User.PASSWORD);

        if (passwordEncoder.matches(savePassword.getOldPassword(), password)) {
            List<UpdateValue> values = new ArrayList<>();

            values.add(
                    new UpdateValue<>(
                            User.PASSWORD,
                            (preparedStatement, index) -> preparedStatement.setString(index, savePassword.getNewPassword())
                    )
            );

            generalDao.update(User.TABLE, values, new AndCondition() {{
                add(new Equals(new ColumnSpec(User.ID), new Param()));
            }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, user.getId())), null);

            return HttpStatus.OK;
        }

        return HttpStatus.BAD_REQUEST;
    }

    @Override
    public SendVerificationResult restorePasswordStart(HttpServletRequest request,
                                           Locale locale,
                                           AuthKey authKey) throws MessagingException {
        User actual = null;

        switch (authKey.getType()) {
            case PHONE:
                actual = userDao.getByPhone(authKey.formattedNumber());
                break;
            case EMAIL:
                actual = userDao.getByEmail(authKey.getEmail());
                break;
        }

        if (actual == null) {
            return new SendVerificationResult(HttpStatus.NOT_FOUND, null, null);
        }
        verificationStorage.expire(request);
        verificationStorage.setAttr(request, VerificationStorage.STATE, SessionState.RESTORE_PASSWORD);
        verificationStorage.setAttr(request, VerificationStorage.FIRST_NAME, actual.getBiography().getFirstName());

        AuthKey phoneKey = AuthKeyArgumentResolver.resolve(actual.getPhone());

        return verificationService.sendVerification(request, locale, phoneKey);
    }

    @Override
    public HttpStatus restorePasswordFinish(HttpServletRequest request, RestorePassword restorePassword) {
        VerificationResult verificationResult = verificationService.verify(
                request,
                restorePassword.getCode(),
                true
        );

        if (verificationResult.isValid()) {
            AuthKey authKey = (AuthKey) verificationStorage.getAttr(request, VerificationStorage.AUTH_KEY, null);

            if (authKey == null) {
                return HttpStatus.BAD_REQUEST;
            }

            List<UpdateValue> values = new ArrayList<>();

            values.add(
                    new UpdateValue<>(
                            User.PASSWORD,
                            (preparedStatement, index) -> preparedStatement.setString(index, passwordEncoder.encode(restorePassword.getPassword()))
                    )
            );
            int updated = generalDao.update(User.TABLE, values, new AndCondition() {{
                add(new Equals(new ColumnSpec(User.PHONE), new Param()));
            }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setString(index, authKey.formattedNumber())), null);

            if (updated == 0) {
                return HttpStatus.NOT_FOUND;
            }

            verificationStorage.expire(request);

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public HttpStatus saveEmailFinish(HttpServletRequest request, AuthenticationKeyConfirmation authenticationKeyConfirmation) {
        User actual = (User) securityService.findLoggedInUser();

        VerificationResult emailVerificationResult = verificationService.verify(
                request,
                authenticationKeyConfirmation.getCode(),
                true
        );

        if (emailVerificationResult.isValid()) {
            AuthKey authKey = (AuthKey) verificationStorage.getAttr(request, VerificationStorage.AUTH_KEY, null);

            if (authKey == null) {
                return HttpStatus.BAD_REQUEST;
            }

            //1. Отвязываем почту у всех людей
            unverifyEmails(authKey.getEmail());

            //1. Обновление email с привязкой данного пользователя
            List<UpdateValue> values = new ArrayList<>();

            values.add(
                    new UpdateValue<>(
                            User.EMAIL,
                            (preparedStatement, index) -> preparedStatement.setString(index, authKey.getEmail())
                    )
            );

            generalDao.update(User.TABLE, values, new AndCondition() {{
                add(new Equals(new ColumnSpec(User.ID), new Param()));
            }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, actual.getId())), null);

            verificationStorage.expire(request);

            actual.setEmail(authKey.getEmail());

            eventPublisher.publishEvent(new ChangeEmailEvent(actual));

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public SendVerificationResult saveEmailStart(HttpServletRequest request, Locale locale, AuthKey authKey) throws MessagingException {
        User user = (User) securityService.findLoggedInUser();

        verificationStorage.expire(request);
        verificationStorage.setAttr(request, VerificationStorage.STATE, SessionState.CHANGE_EMAIL);
        verificationStorage.setAttr(request, VerificationStorage.FIRST_NAME, user.getBiography().getFirstName());

        return verificationService.sendVerification(request, locale, authKey);
    }

    @Override
    public SendVerificationResult savePhoneStart(HttpServletRequest request, Locale locale, AuthKey authKey) throws MessagingException {
        User user = (User) securityService.findLoggedInUser();

        verificationStorage.expire(request);
        verificationStorage.setAttr(request, VerificationStorage.STATE, SessionState.CHANGE_PHONE);
        verificationStorage.setAttr(request, VerificationStorage.FIRST_NAME, user.getBiography().getFirstName());

        return verificationService.sendVerification(request, locale, authKey);
    }

    @Override
    public HttpStatus savePhoneFinish(HttpServletRequest request, AuthenticationKeyConfirmation authenticationKeyConfirmation) {
        User actual = (User) securityService.findLoggedInUser();

        VerificationResult verificationResult = verificationService.verify(
                request,
                authenticationKeyConfirmation.getCode(),
                true
        );

        if (verificationResult.isValid()) {
            AuthKey authKey = (AuthKey) verificationStorage.getAttr(request, VerificationStorage.AUTH_KEY, null);

            if (authKey == null) {
                return HttpStatus.BAD_REQUEST;
            }

            unverifyPhones(authKey.formattedNumber());

            List<UpdateValue> values = new ArrayList<>();

            values.add(
                    new UpdateValue<>(
                            User.PHONE,
                            (preparedStatement, index) -> preparedStatement.setString(index, authKey.formattedNumber())
                    )
            );

            generalDao.update(User.TABLE, values, new AndCondition() {{
                add(new Equals(new ColumnSpec(User.ID), new Param()));
            }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setInt(index, actual.getId())), null);

            verificationStorage.expire(request);

            actual.setPhone(authKey.formattedNumber());

            eventPublisher.publishEvent(new ChangePhoneEvent(actual));

            return HttpStatus.OK;
        }

        return HttpStatus.PRECONDITION_FAILED;
    }

    @Override
    public UserAccount getAccount(TimeZone timeZone, int userId) {
        List<PreparedSetter> values = new ArrayList<>();

        AndCondition andCondition = new AndCondition() {{
            add(new Equals(new ColumnSpec(Biography.USER_ID), new Param()));
        }};

        values.add((preparedStatement, index) -> preparedStatement.setInt(index, userId));

        Biography biography = biographyService.getBiographyByCriteria(timeZone, andCondition, values);

        UserAccount userAccount = new UserAccount();

        userAccount.setBiography(biography);

        return userAccount;
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
                        User.EMAIL,
                        (preparedStatement, index) -> preparedStatement.setNull(index, Types.VARCHAR)
                )
        );

        generalDao.update(User.TABLE, valuesRemoveEmailsVerification, new AndCondition() {{
            add(new Equals(new ColumnSpec(User.EMAIL), new Param()));
        }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setString(index, email)), null);

        eventPublisher.publishEvent(new UnverifyEmailsEvent(email));
    }

    private void unverifyPhones(String phone) {
        List<UpdateValue> valuesRemoveEmailsVerification = new ArrayList<>();

        valuesRemoveEmailsVerification.add(
                new UpdateValue<>(
                        User.PHONE,
                        (preparedStatement, index) -> preparedStatement.setNull(index, Types.VARCHAR)
                )
        );

        generalDao.update(User.TABLE, valuesRemoveEmailsVerification, new AndCondition() {{
            add(new Equals(new ColumnSpec(User.PHONE), new Param()));
        }}, Collections.singletonList((preparedStatement, index) -> preparedStatement.setString(index, phone)), null);

        eventPublisher.publishEvent(new UnverifyPhonesEvent(phone));
    }
}
