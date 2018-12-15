package ru.saidgadjiev.bibliography.configuration;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.model.BiographyCommentResponse;
import ru.saidgadjiev.bibliography.model.BiographyResponse;
import ru.saidgadjiev.bibliography.model.ModerationStatus;

/**
 * Created by said on 16.11.2018.
 */
@Configuration
public class BibliographyConfiguration {

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
                map(source.getParent().getUserName(), destination.getReplyToUserName());
            }
        });

        modelMapper.addMappings(new PropertyMap<Biography, BiographyResponse>() {
            @Override
            protected void configure() {
                using(new Converter<ModerationStatus, Integer>() {
                    @Override
                    public Integer convert(MappingContext<ModerationStatus, Integer> mappingContext) {
                        if (mappingContext.getSource() != null) {
                            return mappingContext.getSource().getCode();
                        }

                        return null;
                    }
                }).map(source.getModerationStatus(), destination.getModerationStatus());
            }
        });

        return modelMapper;
    }
}
