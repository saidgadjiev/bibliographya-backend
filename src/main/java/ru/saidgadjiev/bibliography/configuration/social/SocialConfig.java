package ru.saidgadjiev.bibliography.configuration.social;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import ru.saidgadjiev.bibliography.properties.FacebookProperties;

/**
 * Created by said on 23.12.2018.
 */
@Configuration
public class SocialConfig {

    private final FacebookProperties facebookProperties;

    public SocialConfig(FacebookProperties facebookProperties) {
        this.facebookProperties = facebookProperties;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ConnectionFactoryLocator connectionFactoryLocator() {
        ConnectionFactoryRegistry connectionFactoryLocator = new ConnectionFactoryRegistry();

        connectionFactoryLocator.addConnectionFactory(
                new FacebookConnectionFactory(facebookProperties.getAppId(), facebookProperties.getAppSecret())
        );

        return connectionFactoryLocator;
    }
}
