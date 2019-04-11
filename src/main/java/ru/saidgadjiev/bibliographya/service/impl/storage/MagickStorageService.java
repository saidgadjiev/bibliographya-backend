package ru.saidgadjiev.bibliographya.service.impl.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

import java.nio.file.Path;

/**
 * Created by said on 12/04/2019.
 */
public class MagickStorageService implements StorageService {
    @Override
    public void init() {
        
    }

    @Override
    public void store(String filePath, MultipartFile file) {

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
    public void deleteResource(String filePath) {

    }
}
