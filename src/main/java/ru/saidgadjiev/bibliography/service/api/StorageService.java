package ru.saidgadjiev.bibliography.service.api;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface StorageService {

    void init();

    String store(MultipartFile file);

    Resource loadAsResource(String fileName);

    Path load(String fileName);

    interface UidGenerator {
        int nextUid();
    }
}
