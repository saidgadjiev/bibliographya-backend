package ru.saidgadjiev.bibliographya.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
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

import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by said on 16.11.2018.
 */
@Configuration
public class BibliographyaConfiguration {

    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss.SSS";

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
        slr.setCookieDomain(uiProperties.getName());
        slr.setCookieName("localeInfo");

        return slr;
    }
}
