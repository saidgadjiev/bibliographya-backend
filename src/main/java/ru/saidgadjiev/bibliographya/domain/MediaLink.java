package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 16/04/2019.
 */
public class MediaLink {

    private int id;

    private int objectId;

    private String mediaPath;

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

    public String getMediaPath() {
        return mediaPath;
    }

    public void setMediaPath(String mediaPath) {
        this.mediaPath = mediaPath;
    }
}
