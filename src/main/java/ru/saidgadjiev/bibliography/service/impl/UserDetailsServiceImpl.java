package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliography.dao.UserDao;
import ru.saidgadjiev.bibliography.domain.Role;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.BiographyRequest;
import ru.saidgadjiev.bibliography.model.SignUpRequest;
import ru.saidgadjiev.bibliography.service.api.UserService;

import java.sql.SQLException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by said on 21.10.2018.
 */
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService, UserService {

    private final UserDao userDao;

    private final BiographyService biographyService;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserDetailsServiceImpl(UserDao userDao, BiographyService biographyService, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.biographyService = biographyService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDao.getByUsername(username);
    }

    @Override
    @Transactional
    public void save(SignUpRequest signUpRequest) throws SQLException {
        User user = new ru.saidgadjiev.bibliography.domain.User(
                signUpRequest.getUsername(),
                passwordEncoder.encode(signUpRequest.getPassword()),
                Stream.of(new Role("ROLE_USER")).collect(Collectors.toSet())
        );

        userDao.save(user);
        BiographyRequest biographyRequest = new BiographyRequest();

        biographyRequest.setFirstName(signUpRequest.getFirstName());
        biographyRequest.setLastName(signUpRequest.getLastName());
        biographyRequest.setMiddleName(signUpRequest.getMiddleName());
        biographyRequest.setUserName(signUpRequest.getUsername());

        biographyService.create(biographyRequest);
    }

    @Override
    public boolean isExistUserName(String username) {
        return userDao.isExistUsername(username);
    }
}
