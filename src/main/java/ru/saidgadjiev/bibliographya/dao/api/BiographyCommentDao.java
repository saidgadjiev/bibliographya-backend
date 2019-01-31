package ru.saidgadjiev.bibliographya.dao.api;

import org.springframework.data.domain.Sort;
import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.domain.BiographyComment;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 07.01.2019.
 */
public interface BiographyCommentDao {
    BiographyComment create(BiographyComment biographyComment);

    int delete(int biographyId, int commentId);

    List<BiographyComment> getComments(int biographyId, Sort sort, int limit, long offset);

    long countOffByBiographyId(int biographyId);

    Map<Integer, Long> countOffByBiographiesIds(Collection<Integer> biographiesIds);

    BiographyComment getById(int id);

    List<Map<String, Object>> getFields(Collection<String> fields, Collection<FilterCriteria> criteria);

    int updateContent(Integer commentId, String content);
}
