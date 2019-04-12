package ru.saidgadjiev.bibliographya.service.impl.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;

import java.nio.file.Paths;

@Service
@Qualifier("category")
@Profile({ BibliographyaConfiguration.PROFILE_DEV, BibliographyaConfiguration.PROFILE_PROD})
public class CategoryStorageService extends FileSystemStorageService {

    public CategoryStorageService(StorageProperties storageProperties) {
        super(Paths.get(storageProperties.getRoot()).resolve(storageProperties.getCategoryRoot()));
    }
}
