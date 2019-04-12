package ru.saidgadjiev.bibliographya.service.impl.storage;

import org.apache.commons.lang.SystemUtils;
import org.im4java.process.ProcessStarter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by said on 12/04/2019.
 */
@Service
@Qualifier("magick")
@Profile({ BibliographyaConfiguration.PROFILE_DEV, BibliographyaConfiguration.PROFILE_PROD})
public class MagickStorageService extends FileSystemStorageService {

    public MagickStorageService(StorageProperties storageProperties) {
        super(Paths.get(storageProperties.getRoot()).resolve(storageProperties.getShareRoot()));
    }

    @Override
    public void init() {
        super.init();

        if (SystemUtils.OS_NAME.startsWith("Windows")) {
            ProcessStarter.setGlobalSearchPath("C:\\Program Files\\ImageMagick-7.0.8-Q16");
        }
    }

    @Override
    public void store(String filePath, MultipartFile file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource loadAsResource(String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Path load(String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteResource(String filePath) {
        throw new UnsupportedOperationException();
    }
}
