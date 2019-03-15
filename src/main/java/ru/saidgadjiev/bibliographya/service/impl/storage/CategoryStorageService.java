package ru.saidgadjiev.bibliographya.service.impl.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;

import java.nio.file.Paths;

@Service
@Profile({ "dev", "prod" })
public class CategoryStorageService extends FileSystemStorageService {

    public CategoryStorageService(StorageProperties storageProperties) {
        super(Paths.get(storageProperties.getRoot()).resolve(storageProperties.getCategoryRoot()));
    }
}
