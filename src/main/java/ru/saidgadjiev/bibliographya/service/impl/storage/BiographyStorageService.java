package ru.saidgadjiev.bibliographya.service.impl.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;

import java.nio.file.Paths;

@Service
@Qualifier("biography")
public class BiographyStorageService extends FileSystemStorageService {

    public BiographyStorageService(StorageProperties storageProperties) {
        super(Paths.get(storageProperties.getRoot()).resolve(storageProperties.getBiographyRoot()));
    }
}
