package ru.saidgadjiev.bibliography.configuration;

import org.apache.commons.lang.StringUtils;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.domain.BiographyFix;
import ru.saidgadjiev.bibliography.domain.BiographyReport;
import ru.saidgadjiev.bibliography.model.BiographyCommentResponse;
import ru.saidgadjiev.bibliography.model.BiographyComplaintResponse;
import ru.saidgadjiev.bibliography.model.BiographyFixResponse;
import ru.saidgadjiev.bibliography.model.BiographyResponse;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
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
                Converter<Collection<BiographyReport>, Map<Integer, BiographyComplaintResponse>> converter = context -> {
                    if (context.getSource() != null) {
                        Map<Integer, BiographyComplaintResponse> result = new HashMap<>();

                        for (BiographyReport complaint: context.getSource()) {
                            result.putIfAbsent(complaint.getReason().getCode(), new BiographyComplaintResponse());

                            BiographyComplaintResponse complaintResponse = result.get(complaint.getReason().getCode());

                            complaintResponse.setCount(complaintResponse.getCount() + 1);
                            complaintResponse.setReason(complaint.getReason().getCode());
                            if (StringUtils.isNotBlank(complaint.getReasonText())) {
                                complaintResponse.addComplainText(complaint.getReasonText());
                            }
                        }

                        return result;
                    }

                    return null;
                };
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

                using(converter).map(source.getCreatorBiography(), destination.getCreatorBiography());
                using(converter).map(source.getFixerBiography(), destination.getFixerBiography());
                using(converter).map(source.getBiography(), destination.getBiography());
            }
        });

        return modelMapper;
    }
}
