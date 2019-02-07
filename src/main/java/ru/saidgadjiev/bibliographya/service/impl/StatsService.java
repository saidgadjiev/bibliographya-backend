package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.domain.Stats;

/**
 * Created by said on 02.02.2019.
 */
@Service
public class StatsService {

    private BiographyService biographyService;

    private BiographyLikeService biographyLikeService;

    private BiographyCommentService biographyCommentService;

    private UserService userService;

    @Autowired
    public void setBiographyService(BiographyService biographyService) {
        this.biographyService = biographyService;
    }

    @Autowired
    public void setBiographyLikeService(BiographyLikeService biographyLikeService) {
        this.biographyLikeService = biographyLikeService;
    }

    @Autowired
    public void setBiographyCommentService(BiographyCommentService biographyCommentService) {
        this.biographyCommentService = biographyCommentService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public Stats getStats() {
        Stats stats = new Stats();

        stats.setBiographiesStats(biographyService.getStats());
        stats.setUsersStats(userService.getStats());
        stats.setCommentsStats(biographyCommentService.getStats());
        stats.setLikesStats(biographyLikeService.getStats());

        return stats;
    }
}
