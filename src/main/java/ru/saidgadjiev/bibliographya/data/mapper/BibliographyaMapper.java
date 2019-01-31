package ru.saidgadjiev.bibliographya.data.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.saidgadjiev.bibliographya.domain.*;
import ru.saidgadjiev.bibliographya.model.*;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
@DecoratedWith(BibliographyaMapperDecorator.class)
public interface BibliographyaMapper {

    BiographyResponse convertToBiographyResponse(Biography biography);

    List<BiographyResponse> convertToBiographyResponse(List<Biography> biographies);

    BiographyModerationResponse convertToBiographyModerationResponse(Biography biography);

    List<BiographyModerationResponse> convertToBiographyModerationResponse(List<Biography> biographies);

    List<MyBiographyResponse> convertToMyBiographyResponse(List<Biography> biographies);

    MyBiographyResponse convertToMyBiographyResponse(Biography biography);

    ShortBiographyResponse convertToShortBiographyResponse(Biography biography);

    BiographyCommentResponse convertToBiographyCommentResponse(BiographyComment biographyComment);

    List<BiographyCommentResponse> convertToBiographyCommentResponse(List<BiographyComment> biographyComments);

    BiographyFixResponse convertToBiographyFixResponse(BiographyFix biographyFix);

    List<BiographyFixResponse> convertToBiographyFixResponse(List<BiographyFix> biographyFixes);

    @Mapping(source = "providerType.id", target = "providerId")
    UserResponse convertToUserResponse(User user);

    List<UserResponse> convertToUserResponse(List<User> users);

    default int convertPublishStatus(Biography.PublishStatus publishStatus) {
        return publishStatus.getCode();
    }

    default int convertModerationStatus(Biography.ModerationStatus moderationStatus) {
        return moderationStatus.getCode();
    }

    default int convertFixStatus(BiographyFix.FixStatus fixStatus) {
        return fixStatus.getCode();
    }

    default Collection<String> convertRoles(Set<Role> roles) {
        return roles.stream().map(Role::getName).collect(Collectors.toList());
    }
}
