package ru.saidgadjiev.bibliography.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.pusher.Channel;
import ru.saidgadjiev.bibliography.pusher.Event;

/**
 * Created by said on 05.01.2019.
 */
@Service
public class LikesPusherService {

    private final PusherService pusherService;

    @Autowired
    public LikesPusherService(PusherService pusherService) {
        this.pusherService = pusherService;
    }

    public void addLike(int biographyId, String socketId) {
        pusherService.trigger(Channel.BIOGRAPHY.getName(biographyId), Event.LIKE_ADDED.getDesc(), "", socketId);
    }

    public void deleteLike(int biographyId, String socketId) {
        pusherService.trigger(Channel.BIOGRAPHY.getName(biographyId), Event.LIKE_DELETED.getDesc(), "", socketId);
    }
}
