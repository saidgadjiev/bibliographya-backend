package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public void store(String filePath, MultipartFile file) {
    }

    @Override
    public String move(String filePath, AtomicBoolean exist) {
        return null;
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
