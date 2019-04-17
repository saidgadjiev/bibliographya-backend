package ru.saidgadjiev.bibliographya.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;

public class FileNameUtils {

    private FileNameUtils() {}

    public static String categoryUploadPath(int id, MultipartFile file) {
        String name = String.valueOf(id);
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());

        return StorageProperties.CATEGORY_ROOT + "/" + name + "." + ext;
    }
}
