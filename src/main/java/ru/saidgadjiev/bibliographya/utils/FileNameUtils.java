package ru.saidgadjiev.bibliographya.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileNameUtils {

    private FileNameUtils() {}

    public static String categoryUploadPath(int id, MultipartFile file) {
        String name = String.valueOf(id);
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());

        return StorageProperties.CATEGORY_ROOT + File.separator + name + "." + ext;
    }

    public static String biographyUploadPath(MultipartFile file) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());

        return new SimpleDateFormat(
                "'" + StorageProperties.BIOGRAPHY_ROOT + "/upload_'yyyyMMddHHmmSSSSS'." + ext + "'"
        ).format(new Date());
    }
}
