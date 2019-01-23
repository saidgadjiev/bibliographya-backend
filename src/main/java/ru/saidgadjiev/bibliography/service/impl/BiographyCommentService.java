package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliography.dao.api.BiographyCommentDao;
import ru.saidgadjiev.bibliography.data.FilterCriteria;
import ru.saidgadjiev.bibliography.data.FilterOperation;
import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
import ru.saidgadjiev.bibliography.domain.User;
import ru.saidgadjiev.bibliography.model.BiographyCommentRequest;

import java.sql.PreparedStatement;
import java.util.*;

/**
 * Created by said on 18.11.2018.
 */
@Service
public class BiographyCommentService {

    private final SecurityService securityService;

    private final BiographyCommentDao biographyCommentDao;

    @Autowired
    public BiographyCommentService(SecurityService securityService,
                                   @Qualifier("sql") BiographyCommentDao biographyCommentDao) {
        this.securityService = securityService;
        this.biographyCommentDao = biographyCommentDao;
    }

    @Transactional
    public BiographyComment addComment(int biographyId, BiographyCommentRequest commentRequest) {
        User userDetails = (User) securityService.findLoggedInUser();
        BiographyComment biographyComment = new BiographyComment();

        biographyComment.setContent(commentRequest.getContent());
        biographyComment.setBiographyId(biographyId);
        biographyComment.setUserId(userDetails.getId());

        Biography biography = new Biography();

        biography.setFirstName(userDetails.getBiography().getFirstName());
        biography.setLastName(userDetails.getBiography().getLastName());

        biographyComment.setBiography(biography);

        if (commentRequest.getParent() != null) {
            biographyComment.setParentId(commentRequest.getParent().getId());
            BiographyComment parent = new BiographyComment();

            parent.setId(commentRequest.getParent().getId());

            Biography replyTo = new Biography();

            replyTo.setFirstName(commentRequest.getParent().getFirstName());
            replyTo.setLastName(commentRequest.getParent().getLastName());

            parent.setBiography(replyTo);

            biographyComment.setParent(parent);
        }

        return biographyCommentDao.create(biographyComment);
    }

    @Transactional
    public void deleteComment(int biographyId, int commentId) {
        biographyCommentDao.delete(biographyId, commentId);
    }

    public Page<BiographyComment> getComments(int biographyId, Pageable pageRequest) {
        List<BiographyComment> biographyComments = biographyCommentDao.getComments(
                biographyId,
                pageRequest.getSort(),
                pageRequest.getPageSize(),
                pageRequest.getOffset(),
                null
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

    @Transactional
    public int updateComment(Integer commentId, String content) {
        int updated = biographyCommentDao.updateContent(commentId, content);

        //firebaseCommentDao.updateContent(commentId, content);

        return updated;
    }

    public boolean isIAuthor(int commentId) {
        List<Map<String, Object>> result = biographyCommentDao.getFields(Collections.singletonList("user_id"), Collections.singletonList(
                new FilterCriteria.Builder<Integer>()
                        .propertyName("id")
                        .filterOperation(FilterOperation.EQ)
                        .valueSetter(PreparedStatement::setInt)
                        .filterValue(commentId)
                        .build()
        ));

        if (result.isEmpty()) {
            return false;
        }
        Integer creatorId = (Integer) result.iterator().next().get("user_id");
        User user = (User) securityService.findLoggedInUser();

        return Objects.equals(creatorId, user.getId());
    }
}
