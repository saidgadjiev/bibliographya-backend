package ru.saidgadjiev.bibliography.service.impl.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliography.properties.StorageProperties;
import ru.saidgadjiev.bibliography.service.api.StorageService;

import java.nio.file.Path;

@Service
public class ResourceStorageService implements StorageService {

    private final StorageProperties storageProperties;

    @Autowired
    public ResourceStorageService(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    @Override
    public void init() {

    }

    @Override
    public String store(MultipartFile file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource loadAsResource(String fileName) {
        String dir = storageProperties.getDir();
        Resource resource = new ClassPathResource((dir.endsWith("/") ? dir : dir + "/") + fileName);

        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new StorageFileNotFoundException("Could not read file: " + fileName);
        }
    }

    @Override
    public Path load(String fileName) {
        throw new UnsupportedOperationException();
    }
}
