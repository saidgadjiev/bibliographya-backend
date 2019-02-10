package ru.saidgadjiev.bibliographya.service.api;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {

    void init();

    String storeToCategoryRoot(int id, MultipartFile file);

    Resource loadFromCategoryRootAsResource(String filePath);

    void deleteCategoryResource(String filePath);

    Resource loadAsResource(String filePath);

    Path load(String filePath);

    void deleteResource(String path);

    interface UidGenerator {
        int nextUid();
    }
}
