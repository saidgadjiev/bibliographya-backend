package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 16/04/2019.
 */
public class MediaLink {

    private int id;

    private int objectId;

    private int mediaId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }
}
