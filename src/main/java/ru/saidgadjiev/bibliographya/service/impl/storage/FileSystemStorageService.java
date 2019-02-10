package ru.saidgadjiev.bibliographya.service.impl.storage;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@Profile({ "dev", "prod" })
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    private final Path categoryRootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties storageProperties) {
        this.rootLocation = Paths.get(storageProperties.getRoot());
        this.categoryRootLocation = rootLocation.resolve(storageProperties.getCategoryRoot());
    }

    @Override
    public String storeToCategoryRoot(int id, MultipartFile file) {
        String name = String.valueOf(id);
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        String fileName = name + "." + ext;

        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to storeToCategoryRoot empty file " + fileName);
            }
            if (fileName.contains("..")) {
                throw new StorageException(
                        "Cannot storeToCategoryRoot file with relative path outside current directory "
                                + fileName);
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, categoryRootLocation.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);

                return fileName;
            }
        } catch (IOException e) {
            throw new StorageException("Failed to storeToCategoryRoot file " + fileName, e);
        }
    }

    @Override
    public Resource loadFromCategoryRootAsResource(String filePath) {
        try {
            Path file = categoryRootLocation.resolve(filePath);
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
    public void deleteCategoryResource(String filePath) {
        try {
            Files.delete(categoryRootLocation.resolve(filePath));
        } catch (IOException e) {
            throw new StorageException(e.getMessage(), e);
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
        return rootLocation.resolve(filePath);
    }

    @Override
    public void deleteResource(String path) {
        try {
            Files.delete(rootLocation.resolve(path));
        } catch (IOException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
            Files.createDirectories(categoryRootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
