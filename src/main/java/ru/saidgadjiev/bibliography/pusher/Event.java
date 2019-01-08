package ru.saidgadjiev.bibliography.pusher;

/**
 * Created by said on 04.01.2019.
 */
public enum Event {

    COMMENT_ADDED("comment-added"),
    LIKE_ADDED("like-added"),
    LIKE_DELETED("like-deleted");

    private final String desc;

    Event(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
