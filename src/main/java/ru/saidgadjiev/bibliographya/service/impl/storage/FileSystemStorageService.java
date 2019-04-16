package ru.saidgadjiev.bibliographya.service.impl.storage;

import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@Profile({"dev", "prod"})
public class FileSystemStorageService implements StorageService {

    private final Path path;

    public FileSystemStorageService(StorageProperties storageProperties) {
        this.path = Paths.get(storageProperties.getRoot());
    }

    @Override
    public void store(String filePath, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to storeToCategoryRoot empty file " + filePath);
            }
            if (filePath.contains("..")) {
                throw new StorageException(
                        "Cannot storeToCategoryRoot file with relative path outside current directory "
                                + filePath);
            }
            Path fullPath = path.resolve(filePath);

            Files.createDirectories(fullPath);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to storeToCategoryRoot file " + filePath, e);
        }
    }

    @Override
    public Resource loadAsResource(String filePath) {
        try {
            Path file = load(filePath);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filePath);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filePath, e);
        }
    }

    @Override
    public Path load(String filePath) {
        return path.resolve(filePath);
    }

    @Override
    public void deleteResource(String filePath) {
        try {
            Files.deleteIfExists(path.resolve(filePath));
        } catch (IOException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
