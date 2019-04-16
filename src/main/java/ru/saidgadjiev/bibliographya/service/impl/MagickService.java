package ru.saidgadjiev.bibliographya.service.impl;

import org.apache.commons.lang.StringUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.Operation;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.properties.AppProperties;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class MagickService {

    private StorageProperties storageProperties;

    private AppProperties appProperties;

    public MagickService(StorageProperties storageProperties,
                         AppProperties appProperties) {
        this.storageProperties = storageProperties;
        this.appProperties = appProperties;
    }

    public String createShareImg(String providerId, String magickText, String magickPointSize, String magickSize) throws Exception {
        ConvertCmd convertCmd = new ConvertCmd();

        Operation operation = new Operation();

        operation.addImage(appProperties.getResources() + "/" + providerId + "-" + AppProperties.SHARE_PATH);
        if (StringUtils.isNotBlank(magickPointSize)) {
            operation.addRawArgs("-pointsize", magickPointSize);
        } else {
            operation.addRawArgs("-pointsize", "70");
        }
        if (StringUtils.isNotBlank(magickSize)) {
            operation.addRawArgs("-resize", magickSize);
        }
        operation.addRawArgs("-font", "Roboto-Black");
        operation.addRawArgs("-gravity", "Center");
        operation.addRawArgs("-annotate", "0");
        operation.addRawArgs(magickText);

        Path resultPath = Paths.get(storageProperties.getRoot()).resolve(StorageProperties.TEMP_ROOT);

        Files.createDirectories(resultPath);

        File file = File.createTempFile("share.", ".png", resultPath.toFile());

        operation.addImage(file.getAbsolutePath());

        convertCmd.run(operation);

        return StorageProperties.TEMP_ROOT + "/" + file.getName();
    }
}
