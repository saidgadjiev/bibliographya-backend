package ru.saidgadjiev.bibliographya.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import ru.saidgadjiev.bibliographya.dao.dialect.Dialect;
import ru.saidgadjiev.bibliographya.dao.dialect.H2Dialect;
import ru.saidgadjiev.bibliographya.dao.dialect.PostgresDialect;
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.security.cache.BibliographyaUserCache;
import ru.saidgadjiev.bibliographya.security.cache.BibliographyaUserCacheImpl;

import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by said on 16.11.2018.
 */
@Configuration
public class BibliographyaConfiguration {

    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss.SSS";

    public static final String PROFILE_PROD = "prod";

    public static final String PROFILE_DEV = "dev";

    public static final String PROFILE_TEST = "test";

    private UIProperties uiProperties;

    private DataSourceProperties dataSourceProperties;

    @Autowired
    public BibliographyaConfiguration(UIProperties uiProperties, DataSourceProperties dataSourceProperties) {
        this.uiProperties = uiProperties;
        this.dataSourceProperties = dataSourceProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Dialect dialect() {
        if (dataSourceProperties.getDriverClassName().equals("org.h2.Driver")) {
            return new H2Dialect();
        }

        return new PostgresDialect();
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver slr = new CookieLocaleResolver();

        slr.setDefaultLocale(new Locale("ru", "RU"));
        slr.setDefaultTimeZone(TimeZone.getTimeZone("Europe/Moscow"));
        slr.setCookieDomain(uiProperties.getHost());
        slr.setCookieName("localeInfo");

        return slr;
    }

    @Bean
    public Cache userNativeCache() {
        return new CaffeineCache(
                "userCache",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .expireAfterWrite(1, TimeUnit.MINUTES)
                        .build()
        );
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public BibliographyaUserCache userCache(Cache cache) {
        return new BibliographyaUserCacheImpl(cache);
    }
}
