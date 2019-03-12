package ru.saidgadjiev.bibliographya.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.security.cache.BibliographyaUserCache;
import ru.saidgadjiev.bibliographya.security.filter.AuthenticationFilter;
import ru.saidgadjiev.bibliographya.security.filter.JwtFilter;
import ru.saidgadjiev.bibliographya.security.handler.*;
import ru.saidgadjiev.bibliographya.security.provider.JwtTokenAuthenticationProvider;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.impl.TokenService;

import javax.servlet.Filter;

/**
 * Created by said on 21.10.2018.
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private UserDetailsService userDetailsService;

    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;

    private UserCache userCache;

    private UIProperties uiProperties;

    private JwtProperties jwtProperties;

    private ApplicationEventPublisher eventPublisher;

    private TokenService tokenService;

    @Autowired
    public SecurityConfiguration(UserDetailsService userDetailsService,
                                 PasswordEncoder passwordEncoder,
                                 ObjectMapper objectMapper,
                                 UserCache userCache,
                                 UIProperties uiProperties,
                                 JwtProperties jwtProperties,
                                 ApplicationEventPublisher eventPublisher,
                                 TokenService tokenService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.userCache = userCache;
        this.uiProperties = uiProperties;
        this.jwtProperties = jwtProperties;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                    .anyRequest().permitAll()
                .and()
                    .addFilterAt(new JwtFilter(tokenService, jwtProperties, jwtAuthenticationProvider()), BasicAuthenticationFilter.class)
                    .addFilterAt(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                    .accessDeniedHandler(new Http403AccessDeniedEntryPoint())
                .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .logout()
                    .logoutUrl("/api/auth/signOut")
                    .logoutSuccessHandler(new LogoutSuccessHandlerImpl(eventPublisher))
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID")
                    .addLogoutHandler(new JwtCookieClearingLogoutHandler(uiProperties, jwtProperties))
                    .permitAll();
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


    @Bean
    public Filter authenticationFilter() throws Exception {
        AuthenticationFilter authenticationFilter = new AuthenticationFilter(objectMapper);

        authenticationFilter.setAuthenticationSuccessHandler(
                new AuthenticationSuccessHandlerImpl(
                        objectMapper,
                        tokenService,
                        uiProperties,
                        jwtProperties
                )
        );
        authenticationFilter.setAuthenticationFailureHandler(new AuthenticationFailureHandlerImpl());
        authenticationFilter.setAuthenticationManager(authenticationManager());

        return authenticationFilter;
    }
}
