package ru.saidgadjiev.bibliography.model.firebase;

/**
 * Created by said on 08.01.2019.
 */
public class BiographyStats {

    private int id;

    private long likesCount;

    private long commentsCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(long likesCount) {
        this.likesCount = likesCount;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }
}
