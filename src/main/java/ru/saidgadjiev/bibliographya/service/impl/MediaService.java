package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.MediaDao;
import ru.saidgadjiev.bibliographya.domain.Media;
import ru.saidgadjiev.bibliographya.domain.MediaLink;

import java.util.List;

@Service
public class MediaService {

    private MediaDao mediaDao;

    @Autowired
    public MediaService(MediaDao mediaDao) {
        this.mediaDao = mediaDao;
    }

    public Media create(String path) {
        Media media = new Media();

        media.setPath(path);
        mediaDao.create(media);

        return media;
    }

    public void createLink(int objectId, Media media) {
        MediaLink mediaLink = new MediaLink();

        mediaLink.setMediaId(media.getId());
        mediaLink.setObjectId(objectId);
        mediaDao.createLink(mediaLink);
    }

    public void delete(int mediaId) {
        mediaDao.deleteById(mediaId);
    }

    public List<Media> getNonLinkedMedias() {
        return mediaDao.getNonLinked();
    }

    public List<MediaLink> getLinks(int objectId) {

    }
}
