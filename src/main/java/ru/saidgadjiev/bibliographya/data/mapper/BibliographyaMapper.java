package ru.saidgadjiev.bibliographya.data.mapper;

import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
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

    List<ShortBiographyResponse> convertToShortBiographyResponse(List<BiographyLike> biographyLikes);

    UserResponse convertToUserResponse(User user);

    List<UserResponse> convertToUserResponse(List<User> users);

    BugResponse convertToBugResponse(Bug bug);

    List<BugResponse> convertToBugResponse(List<Bug> bugs);

    default ShortBiographyResponse convertToShortBiographyResponse(BiographyLike biographyLike) {
        if (biographyLike == null) {
            return null;
        }

        ShortBiographyResponse shortBiographyResponse = new ShortBiographyResponse();

        shortBiographyResponse.setId(biographyLike.getUser().getId());
        shortBiographyResponse.setFirstName(biographyLike.getUser().getFirstName());
        shortBiographyResponse.setLastName(biographyLike.getUser().getLastName());

        return shortBiographyResponse;
    }

    default Integer convertPublishStatus(Biography.PublishStatus publishStatus) {
        return publishStatus == null ? null : publishStatus.getCode();
    }

    default Integer convertModerationStatus(Biography.ModerationStatus moderationStatus) {
        return moderationStatus == null ? null : moderationStatus.getCode();
    }

    default Integer convertFixStatus(BiographyFix.FixStatus fixStatus) {
        return fixStatus == null ? null : fixStatus.getCode();
    }

    default Integer convertBugStatus(Bug.BugStatus bugStatus) {
        return bugStatus == null ? null : bugStatus.getCode();
    }

    default Collection<String> convertRoles(Set<Role> roles) {
        return roles == null ? null : roles.stream().map(Role::getName).collect(Collectors.toList());
    }
}
