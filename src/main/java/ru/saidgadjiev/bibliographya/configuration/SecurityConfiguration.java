package ru.saidgadjiev.bibliographya.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import ru.saidgadjiev.bibliographya.security.cache.BibliographyaUserCache;
import ru.saidgadjiev.bibliographya.security.filter.JwtFilter;
import ru.saidgadjiev.bibliographya.security.handler.HttpAuthenticationEntryPoint;
import ru.saidgadjiev.bibliographya.security.handler.Http403AccessDeniedEntryPoint;
import ru.saidgadjiev.bibliographya.security.provider.JwtTokenAuthenticationProvider;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.impl.SessionManager;
import ru.saidgadjiev.bibliographya.service.impl.auth.AuthService;

/**
 * Created by said on 21.10.2018.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private UserDetailsService userDetailsService;

    private AuthService authService;

    private PasswordEncoder passwordEncoder;

    private SessionManager sessionManager;

    private ObjectMapper objectMapper;

    private UserCache userCache;

    @Autowired
    public SecurityConfiguration(UserDetailsService userDetailsService,
                                 PasswordEncoder passwordEncoder,
                                 SessionManager sessionManager,
                                 ObjectMapper objectMapper,
                                 UserCache userCache) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.sessionManager = sessionManager;
        this.objectMapper = objectMapper;
        this.userCache = userCache;
    }

    @Autowired
    public void setAuthService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(jwtAuthenticationProvider()).authenticationProvider(daoAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                .antMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().permitAll()
                .and()
                .addFilterAfter(new JwtFilter(authService), BasicAuthenticationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(new HttpAuthenticationEntryPoint(sessionManager, objectMapper))
                .accessDeniedHandler(new Http403AccessDeniedEntryPoint())
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    private AuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);

        return authProvider;
    }


    private AuthenticationProvider jwtAuthenticationProvider() {
        JwtTokenAuthenticationProvider authProvider = new JwtTokenAuthenticationProvider();

        authProvider.setUserDetailsService((BibliographyaUserDetailsService) userDetailsService);
        authProvider.setUserCache((BibliographyaUserCache) userCache);

        return authProvider;
    }
}
