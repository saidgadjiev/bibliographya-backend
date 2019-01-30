package ru.saidgadjiev.bibliographya.configuration;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.*;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by said on 16.11.2018.
 */
@Configuration
public class BibliographyaConfiguration {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        modelMapper.addMappings(new PropertyMap<BiographyComment, BiographyCommentResponse>() {
            @Override
            protected void configure() {
                map(source.getBiographyId(), destination.getBiographyId());
                map(source.getBiography().getFirstName(), destination.getFirstName());
                map(source.getBiography().getLastName(), destination.getLastName());
                map(source.getParent().getBiographyId(), destination.getReplyToBiographyId());
                map(source.getParent().getBiography().getFirstName(), destination.getReplyToFirstName());
                map(source.getParent().getUserId(), destination.getReplyToUserId());
            }
        });

        modelMapper.addMappings(new PropertyMap<Biography, BiographyResponse>() {
            @Override
            protected void configure() {
                using(new Converter<Biography.ModerationStatus, Integer>() {
                    @Override
                    public Integer convert(MappingContext<Biography.ModerationStatus, Integer> mappingContext) {
                        if (mappingContext.getSource() != null) {
                            return mappingContext.getSource().getCode();
                        }

                        return null;
                    }
                }).map(source.getModerationStatus(), destination.getModerationStatus());
                using(new Converter<Biography.PublishStatus, Integer>() {
                    @Override
                    public Integer convert(MappingContext<Biography.PublishStatus, Integer> context) {
                        if (context.getSource() != null) {
                            return context.getSource().getCode();
                        }

                        return null;
                    }
                }).map(source.getPublishStatus(), destination.getPublishStatus());
            }
        });

        modelMapper.addMappings(new PropertyMap<BiographyFix, BiographyFixResponse>() {
            @Override
            protected void configure() {
                using(new Converter<BiographyFix.FixStatus, Integer>() {
                    @Override
                    public Integer convert(MappingContext<BiographyFix.FixStatus, Integer> mappingContext) {
                        if (mappingContext.getSource() != null) {
                            return mappingContext.getSource().getCode();
                        }

                        return null;
                    }
                }).map(source.getStatus(), destination.getStatus());

                Converter<Biography, BiographyResponse> converter = context -> {
                    if (context.getSource() != null) {
                        BiographyResponse biographyResponse = new BiographyResponse();

                        biographyResponse.setId(context.getSource().getId());
                        biographyResponse.setFirstName(context.getSource().getFirstName());
                        biographyResponse.setLastName(context.getSource().getLastName());
                        biographyResponse.setMiddleName(context.getSource().getMiddleName());
                        biographyResponse.setBiography(context.getSource().getBiography());
                        biographyResponse.setCategories(context.getSource().getCategories());

                        return biographyResponse;
                    }

                    return null;
                };

                using(converter).map(source.getBiography(), destination.getBiography());
            }
        });

        modelMapper.addMappings(new PropertyMap<User, UserResponse>() {
            @Override
            protected void configure() {
                map(source.getId(), destination.getId());
                using(new Converter<ProviderType, String>() {
                    @Override
                    public String convert(MappingContext<ProviderType, String> context) {
                        if (context.getSource() != null) {
                            return context.getSource().getId();
                        }

                        return null;
                    }
                }).map(source.getProviderType(), destination.getProviderId());

                using(new Converter<Biography, BiographyResponse>() {
                    @Override
                    public BiographyResponse convert(MappingContext<Biography, BiographyResponse> context) {
                        if (context.getSource() != null) {
                            BiographyResponse biographyResponse = new BiographyResponse();

                            biographyResponse.setId(context.getSource().getId());
                            biographyResponse.setFirstName(context.getSource().getFirstName());
                            biographyResponse.setLastName(context.getSource().getLastName());
                            biographyResponse.setMiddleName(context.getSource().getMiddleName());

                            return biographyResponse;
                        }

                        return null;
                    }
                }).map(source.getBiography(), destination.getBiography());
                using(new Converter<Set<Role>, Collection<String>>() {
                    @Override
                    public Collection<String> convert(MappingContext<Set<Role>, Collection<String>> context) {
                        if (context.getSource() != null) {
                            return context.getSource().stream().map(Role::getName).collect(Collectors.toList());
                        }

                        return null;
                    }
                }).map(source.getRoles(), destination.getRoles());
            }
        });

        modelMapper.addMappings(new PropertyMap<Biography, ShortBiographyResponse>() {
            @Override
            protected void configure() {
            }
        });

        return modelMapper;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
