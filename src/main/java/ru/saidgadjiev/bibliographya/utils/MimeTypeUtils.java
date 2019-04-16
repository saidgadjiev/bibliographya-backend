package ru.saidgadjiev.bibliographya.utils;

import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MimeTypeUtils {

    private MimeTypeUtils() {}

    public static String getExtension(String mimeType) {
        if (mimeType == null) {
            return null;
        }
        MimeTypes allTypes = MimeTypes.getDefaultMimeTypes();
        MimeType parsedMimeType = null;
        try {
            parsedMimeType = allTypes.forName(mimeType);
        } catch (MimeTypeException e) {
            return null;
        }

        return parsedMimeType.getExtension();
    }

    public static String getMime(String fileName) throws IOException {
		String mime = Files.probeContentType(Paths.get(fileName));
		if (mime == null) {
		    return new MimetypesFileTypeMap().getContentType(fileName);
        }
        return mime;
	}
}
