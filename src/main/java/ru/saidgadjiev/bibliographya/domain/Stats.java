package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 02.02.2019.
 */
public class Stats {

    private UsersStats usersStats;

    private BiographiesStats biographiesStats;

    private CommentsStats commentsStats;

    private LikesStats likesStats;

    public UsersStats getUsersStats() {
        return usersStats;
    }

    public void setUsersStats(UsersStats usersStats) {
        this.usersStats = usersStats;
    }

    public BiographiesStats getBiographiesStats() {
        return biographiesStats;
    }

    public void setBiographiesStats(BiographiesStats biographiesStats) {
        this.biographiesStats = biographiesStats;
    }

    public CommentsStats getCommentsStats() {
        return commentsStats;
    }

    public void setCommentsStats(CommentsStats commentsStats) {
        this.commentsStats = commentsStats;
    }

    public LikesStats getLikesStats() {
        return likesStats;
    }

    public void setLikesStats(LikesStats likesStats) {
        this.likesStats = likesStats;
    }
}
