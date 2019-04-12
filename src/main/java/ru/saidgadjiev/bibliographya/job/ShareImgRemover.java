package ru.saidgadjiev.bibliographya.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Created by said on 12/04/2019.
 */
@Component
public class ShareImgRemover {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShareImgRemover.class);

    private StorageProperties storageProperties;

    public ShareImgRemover(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Scheduled(fixedDelay = 60000)
    public void removeShareImg() throws IOException {
        Path targetPath = Paths.get(storageProperties.getRoot()).resolve(storageProperties.getShareRoot());

        if (Files.notExists(targetPath)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(targetPath)) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        File file = path.toFile();

                        long diff = new Date().getTime() - file.lastModified();

                        if (TimeUnit.MILLISECONDS.toMinutes(diff) > 1) {
                            boolean delete = file.delete();

                            if (!delete) {
                                LOGGER.debug("Share img " + file.getName() + " not deleted.");
                            }
                        }
                    });
        }
    }
}
