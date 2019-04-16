package ru.saidgadjiev.bibliographya.service.impl.storage;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Profile({"dev", "prod"})
public class FileSystemStorageService implements StorageService {

    private static final int DEFAULT_BLOCK_SIZE = 64 * 1024;

    private static final int DEFAULT_CHUNK_SIZE = 10 * 1024 * 1024;

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
    public String move(String filePath, AtomicBoolean exist) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            String ext = FilenameUtils.getExtension(filePath);

            File file = path.resolve(filePath).toFile();

            try (InputStream input = new FileInputStream(file)) {
                byte[] bytes = new byte[DEFAULT_BLOCK_SIZE];
                int read;

                while ((read = input.read(bytes)) != -1) {
                    digest.update(bytes, 0, read);
                }
            }
            byte[] digestBytes = digest.digest();
            String hash = hashToString(digestBytes);
            String newFilePath = getFilePath(hash, ext);

            Path fullPath = path.resolve(newFilePath);

            if (Files.exists(fullPath)) {
                exist.set(true);
                FileUtils.deleteQuietly(file);

                return newFilePath;
            }
            Files.createDirectories(fullPath);

            Files.move(file.toPath(), fullPath);

            exist.set(false);

            return newFilePath;
        } catch (IOException | NoSuchAlgorithmException e) {
            throw new StorageException("Failed to store file", e);
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

    private static String hashToString(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) {
            out.append(Integer.toHexString(b & 0xff));
        }
        return out.toString();
    }

    private String getFilePath(String hash, String ext) {
        StringBuilder builder = new StringBuilder();

        int index = 0;
        for (char c : hash.toCharArray()) {
            if (index % 2 == 0) {
                builder.append(File.separatorChar);
            }
            builder.append(c);
            index++;
        }
        builder.append(".").append(ext);

        return builder.toString();
    }
}
