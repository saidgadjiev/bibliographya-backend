package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.BiographyCommentDao;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.BiographyCommentRequest;
import ru.saidgadjiev.bibliography.security.service.SecurityService;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 18.11.2018.
 */
@Service
public class BiographyCommentService {

    private final SecurityService securityService;

    private final BiographyCommentDao biographyCommentDao;

    @Autowired
    public BiographyCommentService(SecurityService securityService,
                                   BiographyCommentDao biographyCommentDao) {
        this.securityService = securityService;
        this.biographyCommentDao = biographyCommentDao;
    }

    public BiographyComment addComment(int biographyId, BiographyCommentRequest commentRequest) {
        User userDetails = (User) securityService.findLoggedInUser();
        BiographyComment biographyComment = new BiographyComment();

        biographyComment.setContent(commentRequest.getContent());
        biographyComment.setBiographyId(biographyId);
        biographyComment.setUserName(userDetails.getUsername());

        Biography biography = new Biography.Builder()
                .setFirstName(userDetails.getBiography().getFirstName())
                .setLastName(userDetails.getBiography().getLastName())
                .build();

        biographyComment.setBiography(biography);

        if (commentRequest.getParent() != null) {
            biographyComment.setParentId(commentRequest.getParent().getId());
            BiographyComment parent = new BiographyComment();

            parent.setId(commentRequest.getParent().getId());

            Biography replyTo = new Biography.Builder()
                    .setFirstName(commentRequest.getParent().getFirstName())
                    .setUserName(commentRequest.getParent().getLastName())
                    .build();

            parent.setBiography(replyTo);

            biographyComment.setParent(parent);
        }

        return biographyCommentDao.create(biographyComment);
    }

    public void deleteComment(int commentId) {
        biographyCommentDao.delete(commentId);
    }

    public Page<BiographyComment> getComments(int biographyId, Pageable pageRequest) {
        List<BiographyComment> biographyComments = biographyCommentDao.getComments(
                biographyId,
                pageRequest.getSort() == null ? Sort.by(Sort.Direction.ASC, "created_at") : pageRequest.getSort(),
                pageRequest.getPageSize(),
                pageRequest.getOffset()
        );
        long total = biographyCommentDao.countOffByBiographyId(biographyId);

        return new PageImpl<>(biographyComments, pageRequest, total);
    }

    public long getBiographyCommentsCount(int biographyId) {
        return biographyCommentDao.countOffByBiographyId(biographyId);
    }

    public Map<Integer, Long> getBiographiesCommentsCount(Collection<Integer> biographiesIds) {
        return biographyCommentDao.countOffByBiographiesIds(biographiesIds);
    }

    public int updateComment(Integer commentId, String content) {
        return biographyCommentDao.updateContent(commentId, content);
    }
}
