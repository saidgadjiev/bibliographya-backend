package ru.saidgadjiev.bibliographya.utils;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileNameUtils {

    private FileNameUtils() {}

    public static String categoryImagePath(int id, MultipartFile file) {
        String name = String.valueOf(id);
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());

        return name + "." + ext;
    }
}
