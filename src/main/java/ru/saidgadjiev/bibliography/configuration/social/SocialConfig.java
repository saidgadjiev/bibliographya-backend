package ru.saidgadjiev.bibliography.configuration.social;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.support.ConnectionFactoryRegistry;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.vkontakte.connect.VKontakteConnectionFactory;
import ru.saidgadjiev.bibliography.properties.FacebookProperties;
import ru.saidgadjiev.bibliography.properties.VKProperties;

/**
 * Created by said on 23.12.2018.
 */
@Configuration
public class SocialConfig {

    private final FacebookProperties facebookProperties;

    private final VKProperties vkProperties;

    public SocialConfig(FacebookProperties facebookProperties, VKProperties vkProperties) {
        this.facebookProperties = facebookProperties;
        this.vkProperties = vkProperties;
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public ConnectionFactoryLocator connectionFactoryLocator() {
        ConnectionFactoryRegistry connectionFactoryLocator = new ConnectionFactoryRegistry();

        connectionFactoryLocator.addConnectionFactory(
                new FacebookConnectionFactory(facebookProperties.getAppId(), facebookProperties.getAppSecret())
        );
        connectionFactoryLocator.addConnectionFactory(
                new VKontakteConnectionFactory(vkProperties.getAppId(), vkProperties.getAppSecret())
        );

        return connectionFactoryLocator;
    }
}
