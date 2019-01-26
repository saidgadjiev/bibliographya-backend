package ru.saidgadjiev.bibliographya.dao.api;

import ru.saidgadjiev.bibliographya.domain.BiographyLike;

import java.util.Collection;
import java.util.Map;

/**
 * Created by said on 08.01.2019.
 */
public interface BiographyLikeDao {
    int create(BiographyLike like);

    int delete(BiographyLike like);

    int getLikesCount(int biographyId);

    boolean isLiked(int userId, int biographyId);

    Map<Integer, Boolean> isLikedByBiographies(int userId, Collection<Integer> biographiesIds);

    Map<Integer, Integer> getLikesCountByBiographies(Collection<Integer> biographiesIds);
}
