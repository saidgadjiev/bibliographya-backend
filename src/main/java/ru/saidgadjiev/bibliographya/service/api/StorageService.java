package ru.saidgadjiev.bibliographya.service.api;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {

    void init();

    void store(String filePath, MultipartFile file);
    
    String move(String filePath);

    Resource loadAsResource(String filePath);

    Path load(String filePath);

    void deleteResource(String filePath);
}
