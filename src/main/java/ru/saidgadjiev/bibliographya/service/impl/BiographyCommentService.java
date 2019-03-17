package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.saidgadjiev.bibliographya.dao.impl.BiographyCommentDao;
import ru.saidgadjiev.bibliographya.dao.impl.GeneralDao;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyComment;
import ru.saidgadjiev.bibliographya.domain.CommentsStats;
import ru.saidgadjiev.bibliographya.domain.User;
import ru.saidgadjiev.bibliographya.model.BiographyCommentRequest;

import java.sql.PreparedStatement;
import java.util.*;

/**
 * Created by said on 18.11.2018.
 */
@Service
public class BiographyCommentService {

    private final SecurityService securityService;

    private final BiographyCommentDao biographyCommentDao;

    private GeneralDao generalDao;

    @Autowired
    public BiographyCommentService(SecurityService securityService,
                                   BiographyCommentDao biographyCommentDao,
                                   GeneralDao generalDao) {
        this.securityService = securityService;
        this.biographyCommentDao = biographyCommentDao;
        this.generalDao = generalDao;
    }

    @Transactional
    public BiographyComment addComment(TimeZone timeZone, int biographyId, BiographyCommentRequest commentRequest) {
        User userDetails = (User) securityService.findLoggedInUser();
        BiographyComment biographyComment = new BiographyComment();

        biographyComment.setContent(commentRequest.getContent());
        biographyComment.setBiographyId(biographyId);
        biographyComment.setUserId(userDetails.getId());

        Biography biography = new Biography();

        biography.setId(userDetails.getBiography().getId());
        biography.setFirstName(userDetails.getBiography().getFirstName());
        biography.setLastName(userDetails.getBiography().getLastName());

        biographyComment.setUser(biography);

        if (commentRequest.getParentId() != null) {
            biographyComment.setParentId(commentRequest.getParentId());
            Collection<FilterCriteria> criteria = new ArrayList<>();

            criteria.add(
                    new FilterCriteria.Builder<Integer>()
                        .propertyName(BiographyComment.ID)
                        .filterOperation(FilterOperation.EQ)
                        .filterValue(commentRequest.getParentId())
                        .needPreparedSet(true)
                        .valueSetter(PreparedStatement::setInt)
                        .build()
            );

            List<Map<String, Object>> parentCommentValues = generalDao.getFields(
                    BiographyComment.TABLE,
                    Collections.singletonList(BiographyComment.USER_ID),
                    criteria
            );

            biographyComment.setParentUserId((Integer) parentCommentValues.get(0).get(BiographyComment.USER_ID));
        }

        biographyCommentDao.create(biographyComment);

        return biographyCommentDao.getById(timeZone, biographyComment.getId());
    }

    @Transactional
    public void deleteComment(int commentId) {
        biographyCommentDao.delete(commentId);
    }

    public Page<BiographyComment> getComments(TimeZone timeZone, int biographyId, Pageable pageRequest) {
        List<BiographyComment> biographyComments = biographyCommentDao.getComments(
                timeZone,
                biographyId,
                pageRequest.getSort(),
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

    @Transactional
    public int updateComment(Integer commentId, BiographyCommentRequest request) {
        return biographyCommentDao.updateContent(commentId, request.getContent());
    }

    public boolean isIAuthor(int commentId) {
        List<Map<String, Object>> result = generalDao.getFields(
                BiographyComment.TABLE,
                Collections.singletonList("user_id"),
                Collections.singletonList(
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

    public CommentsStats getStats() {
        CommentsStats commentsStats = new CommentsStats();

        commentsStats.setCount(biographyCommentDao.countOff());

        return commentsStats;
    }
}
