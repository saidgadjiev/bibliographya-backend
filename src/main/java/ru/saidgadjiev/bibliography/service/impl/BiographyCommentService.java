package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.BiographyCommentDao;
import ru.saidgadjiev.bibliography.domain.BiographyComment;
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

    public void addComment(int biographyId, BiographyCommentRequest commentRequest) {
        UserDetails userDetails = securityService.findLoggedInUser();
        BiographyComment biographyComment = new BiographyComment();

        biographyComment.setBiographyId(biographyId);
        biographyComment.setContent(commentRequest.getContent());
        biographyComment.setUserName(userDetails.getUsername());
        biographyComment.setParentId(commentRequest.getParentId());

        biographyCommentDao.create(biographyComment);
    }

    public void deleteComment(int biographyId) {
        UserDetails userDetails = securityService.findLoggedInUser();

        BiographyComment biographyComment = new BiographyComment();

        biographyComment.setBiographyId(biographyId);
        biographyComment.setUserName(userDetails.getUsername());

        biographyCommentDao.delete(biographyComment);
    }

    public Page<BiographyComment> getComments(int biographyId, Pageable pageRequest) {
        List<BiographyComment> biographyComments = biographyCommentDao.getComments(
                biographyId,
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
}
