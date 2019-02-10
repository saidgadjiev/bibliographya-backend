package ru.saidgadjiev.bibliographya.service.impl.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

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
    public String storeToCategoryRoot(int id, MultipartFile file) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Resource loadFromCategoryRootAsResource(String filePath) {
        return null;
    }

    @Override
    public void deleteCategoryResource(String filePath) {

    }

    @Override
    public Resource loadAsResource(String filePath) {
        String dir = storageProperties.getRoot();
        Resource resource = new ClassPathResource((dir.endsWith("/") ? dir : dir + "/") + filePath);

        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new StorageFileNotFoundException("Could not read file: " + filePath);
        }
    }

    @Override
    public Path load(String filePath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteResource(String path) {

    }
}
