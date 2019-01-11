package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.dao.api.BiographyLikeDao;
import ru.saidgadjiev.bibliography.dao.impl.BiographyLikeDaoImpl;
import ru.saidgadjiev.bibliography.domain.BiographyLike;
import ru.saidgadjiev.bibliography.domain.User;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by said on 15.11.2018.
 */
@Service
public class BiographyLikeService {

    private final SecurityService securityService;

    private final BiographyLikeDao biographyLikeDao;

    @Autowired
    public BiographyLikeService(SecurityService securityService,
                                @Qualifier("sql") BiographyLikeDao biographyLikeDao) {
        this.securityService = securityService;
        this.biographyLikeDao = biographyLikeDao;
    }

    public void like(int biographyId) {
        User userDetails = (User) securityService.findLoggedInUser();

        BiographyLike biographyLike = new BiographyLike(
                userDetails.getId(),
                biographyId
        );

        biographyLikeDao.create(biographyLike);
    }

    public void unlike(int biographyId) {
        User userDetails = (User) securityService.findLoggedInUser();

        BiographyLike biographyLike = new BiographyLike(
                userDetails.getId(),
                biographyId
        );

        biographyLikeDao.delete(biographyLike);
    }

    public Map<Integer, Integer> getBiographiesLikesCount(Collection<Integer> biographiesIds) {
        return biographyLikeDao.getLikesCountByBiographies(biographiesIds);
    }

    public int getBiographyLikesCount(Integer biographyId) {
        return biographyLikeDao.getLikesCount(biographyId);
    }

    public Map<Integer, Boolean> getBiographiesIsLiked(Collection<Integer> biographiesIds) {
        User userDetails = (User) securityService.findLoggedInUser();

        if (userDetails == null) {
            return biographiesIds
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), integer -> false));
        }

        return biographyLikeDao.isLikedByBiographies(userDetails.getId(), biographiesIds);
    }

    public boolean getBiographyIsLiked(Integer biographyId) {
        User userDetails = (User) securityService.findLoggedInUser();

        if (userDetails == null) {
            return false;
        }

        return biographyLikeDao.isLiked(userDetails.getId(), biographyId);
    }
}
