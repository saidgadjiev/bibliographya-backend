package ru.saidgadjiev.bibliography.dao.api;

import org.springframework.data.domain.Sort;
import ru.saidgadjiev.bibliography.domain.BiographyComment;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by said on 07.01.2019.
 */
public interface BiographyCommentDao {
    BiographyComment create(BiographyComment biographyComment);

    int delete(int biographyId, int commentId);

    List<BiographyComment> getComments(int biographyId, Sort sort, int limit, long offset, Integer afterKey);

    long countOffByBiographyId(int biographyId);

    Map<Integer, Long> countOffByBiographiesIds(Collection<Integer> biographiesIds);

    BiographyComment getById(int id);

    int updateContent(Integer commentId, String content);
}
