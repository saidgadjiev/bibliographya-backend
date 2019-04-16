package ru.saidgadjiev.bibliographya.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.saidgadjiev.bibliographya.domain.Media;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.service.api.StorageService;
import ru.saidgadjiev.bibliographya.service.impl.MediaService;
import ru.saidgadjiev.bibliographya.service.impl.StashImageService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Created by said on 12/04/2019.
 */
@Component
public class MediaCleaner {

    private static final Logger LOGGER = LoggerFactory.getLogger(MediaCleaner.class);

    private StorageProperties storageProperties;

    private StashImageService stashImageService;

    private StorageService storageService;

    private MediaService mediaService;

    public MediaCleaner(StorageProperties storageProperties,
                        StashImageService stashImageService,
                        StorageService storageService,
                        MediaService mediaService) {
        this.storageProperties = storageProperties;
        this.stashImageService = stashImageService;
        this.storageService = storageService;
        this.mediaService = mediaService;
    }

    @Scheduled(cron = "0 */6 * * *")
    public void cleanTempFiles() throws IOException {
        Path basePath = Paths.get(storageProperties.getRoot());
        Path targetPath = Paths.get(storageProperties.getRoot()).resolve(StorageProperties.TEMP_ROOT);

        if (Files.notExists(targetPath)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(targetPath)) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        Path relativePath = path.relativize(basePath);
                        File file = path.toFile();

                        long diff = new Date().getTime() - file.lastModified();

                        if (TimeUnit.MILLISECONDS.toHours(diff) > 6) {
                            boolean delete = file.delete();

                            if (!delete) {
                                LOGGER.debug("Temp img " + file.getName() + " not deleted.");
                            } else {
                                stashImageService.remove(relativePath.toString());
                            }
                        }
                    });
        }
    }


    @Scheduled(cron = "0 */6 * * *")
    public void cleanUserUnusedMedias() {
        List<Media> unusedMedias = mediaService.getNonLinkedMedias();

        for (Media media: unusedMedias) {
            storageService.deleteResource(media.getPath());
            mediaService.delete(media.getId());
        }
    }
}
