package ru.saidgadjiev.bibliographya.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.saidgadjiev.bibliographya.dao.dialect.Dialect;
import ru.saidgadjiev.bibliographya.dao.dialect.H2Dialect;
import ru.saidgadjiev.bibliographya.dao.dialect.PostgresDialect;

/**
 * Created by said on 16.11.2018.
 */
@Configuration
public class BibliographyaConfiguration {

    private DataSourceProperties dataSourceProperties;

    @Autowired
    public BibliographyaConfiguration(DataSourceProperties dataSourceProperties) {
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
}
