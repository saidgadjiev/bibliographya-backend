package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.dao.impl.MediaDao;
import ru.saidgadjiev.bibliographya.domain.Media;
import ru.saidgadjiev.bibliographya.domain.MediaLink;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.properties.UIProperties;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MediaService {

    private StashMediaService stashMediaService;

    private StorageService storageService;

    private UIProperties uiProperties;

    private StorageProperties storageProperties;

    private MediaDao mediaDao;

    @Autowired
    public MediaService(StashMediaService stashMediaService,
                        StorageService storageService,
                        UIProperties uiProperties,
                        StorageProperties storageProperties,
                        MediaDao mediaDao) {
        this.stashMediaService = stashMediaService;
        this.storageService = storageService;
        this.uiProperties = uiProperties;
        this.storageProperties = storageProperties;
        this.mediaDao = mediaDao;
    }

    public Media create(String path) {
        Media media = new Media();

        media.setPath(path);
        mediaDao.create(media);

        return media;
    }

    public void createLink(int objectId, String mediaPth) {
        MediaLink mediaLink = new MediaLink();

        mediaLink.setMediaPath(mediaPth);
        mediaLink.setObjectId(objectId);
        mediaDao.createLink(mediaLink);
    }

    public void removeLink(String mediaPath, int objectId) {
        mediaDao.removeLinkById(mediaPath, objectId);
    }

    public void delete(int mediaId) {
        mediaDao.deleteById(mediaId);
    }

    public List<Media> getNonLinkedMedias() {
        return mediaDao.getNonLinked();
    }

    public List<MediaLink> getLinks(int objectId) {
        return mediaDao.getLinks(objectId);
    }

    public String storeMedia(int id, String bio) throws MalformedURLException {
        List<MediaLink> currentMediaLinks = getLinks(id);
        List<String> actualMediaLinks = new ArrayList<>();
        String result = bio;

        if (StringUtils.isNotBlank(bio)) {
            Document document = Jsoup.parse(bio);

            Elements imgs = document.getElementsByTag("img");

            for (Element img : imgs) {
                URL src = new URL(img.attr("src"));

                if (!src.getHost().equals(uiProperties.getHost())) {
                    continue;
                }
                //1. Получаем относительный путь к файлу https://bibliographya.com/upload/temp/upload.jpg -> temp/upload.jpg
                String relativeSrc = getRelativeSrc(src.getPath());

                if (!relativeSrc.startsWith(StorageProperties.TEMP_ROOT)) {
                    actualMediaLinks.add(relativeSrc);
                    continue;
                }

                String newRelativeSrc = storageService.move(relativeSrc);

                //2. Новый путь к файлу http
                StringBuilder srcBuilder = new StringBuilder();

                srcBuilder.append(src.getProtocol()).append("://").append(src.getHost());

                if (src.getPort() != src.getDefaultPort()) {
                    srcBuilder.append(":").append(src.getPort());
                }

                srcBuilder.append("/").append(storageProperties.getRoot()).append("/").append(newRelativeSrc);

                img.attr("src", srcBuilder.toString());

                createLink(id, create(newRelativeSrc).getPath());

                stashMediaService.remove(relativeSrc);
            }

            result = document.html();
        }
        Map<String, Long> currentMediaLinksCountMap = currentMediaLinks.stream()
                .collect(Collectors.groupingBy(MediaLink::getMediaPath, Collectors.counting()));
        Map<String, Long> actualMediaLinksCountMap = actualMediaLinks.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        for (Map.Entry<String, Long> entry: currentMediaLinksCountMap.entrySet()) {
            long actualCount = actualMediaLinksCountMap.containsKey(entry.getKey()) ? actualMediaLinksCountMap.get(entry.getKey()) : 0;

            for (long i = actualCount; i < entry.getValue(); ++i) {
                removeLink(entry.getKey(), id);
            }
        }

        return result;
    }

    //https://bibliographya.com/upload/temp/upload.jpg -> temp/upload.jpg
    private String getRelativeSrc(String srcPath) {
        int uploadRootIndexOf = srcPath.indexOf(storageProperties.getRoot() + "/");

        return srcPath.substring(uploadRootIndexOf + storageProperties.getRoot().length() + 1);
    }
}
