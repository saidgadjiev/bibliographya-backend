package ru.saidgadjiev.bibliography.service.impl;

import com.pusher.rest.Pusher;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliography.properties.PusherProperties;

/**
 * Created by said on 04.01.2019.
 */
@Service
public class PusherService {

    private final PusherProperties pusherProperties;

    private final Pusher pusher;

    public PusherService(PusherProperties pusherProperties) {
        this.pusherProperties = pusherProperties;
        this.pusher = newInstance();
    }

    public void trigger(String chanel, String eventName, Object data) {
        pusher.trigger(chanel, eventName, data);
    }

    public void trigger(String chanel, String eventName, Object data, String socketId) {
        pusher.trigger(chanel, eventName, data, socketId);
    }

    private Pusher newInstance() {
        Pusher pusher = new Pusher(pusherProperties.getAppId(), pusherProperties.getKey(), pusherProperties.getSecret());

        pusher.setCluster(pusherProperties.getCluster());

        return pusher;
    }
}
