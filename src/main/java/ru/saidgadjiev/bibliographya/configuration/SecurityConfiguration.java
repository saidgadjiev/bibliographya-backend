package ru.saidgadjiev.bibliographya.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
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
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.saidgadjiev.bibliographya.properties.JwtProperties;
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.security.cache.BibliographyaUserCache;
import ru.saidgadjiev.bibliographya.security.filter.AuthenticationFilter;
import ru.saidgadjiev.bibliographya.security.handler.*;
import ru.saidgadjiev.bibliographya.security.provider.JwtTokenAuthenticationProvider;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;
import ru.saidgadjiev.bibliographya.service.impl.AuthTokenService;

import javax.servlet.Filter;
import java.util.Arrays;
import java.util.Collections;

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

    private AuthTokenService tokenService;

    private SecurityContextRepository securityContextRepository;

    @Autowired
    public SecurityConfiguration(UserDetailsService userDetailsService,
                                 PasswordEncoder passwordEncoder,
                                 ObjectMapper objectMapper,
                                 UserCache userCache,
                                 UIProperties uiProperties,
                                 JwtProperties jwtProperties,
                                 ApplicationEventPublisher eventPublisher,
                                 AuthTokenService tokenService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.tokenService = tokenService;
        this.objectMapper = objectMapper;
        this.userCache = userCache;
        this.uiProperties = uiProperties;
        this.jwtProperties = jwtProperties;
    }

    @Autowired
    public void setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(jwtAuthenticationProvider()).authenticationProvider(daoAuthenticationProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                    .configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .authorizeRequests()
                    .antMatchers("/actuator/**").hasAuthority("ROLE_ADMIN")
                    .anyRequest().permitAll()
                .and()
                    .addFilterAt(authenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()
                    .accessDeniedHandler(new Http403AccessDeniedEntryPoint())
                .and()
                    .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .securityContext().securityContextRepository(securityContextRepository)
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
                        jwtProperties,
                        eventPublisher
                )
        );
        authenticationFilter.setAuthenticationFailureHandler(new AuthenticationFailureHandlerImpl());
        authenticationFilter.setAuthenticationManager(authenticationManager());

        return authenticationFilter;
    }

    private CorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Collections.singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setExposedHeaders(Arrays.asList(jwtProperties.tokenName(), HttpHeaders.SET_COOKIE));

        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
