package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

import java.nio.file.Path;

/**
 * Created by said on 10.02.2019.
 */
@Service
@Profile("test")
public class FileSystemStorageService implements StorageService {
    @Override
    public void init() {

    }

    @Override
    public String storeToCategoryRoot(int id, MultipartFile file) {
        return null;
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
        return null;
    }

    @Override
    public Path load(String filePath) {
        return null;
    }

    @Override
    public void deleteResource(String path) {

    }
}
