package ru.saidgadjiev.bibliographya.service.impl;

import org.im4java.core.ConvertCmd;
import org.im4java.core.Operation;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.properties.AppProperties;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;

import java.io.File;
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

    public String createShareImg(String magickText) throws Exception {
        ConvertCmd convertCmd = new ConvertCmd();

        Operation operation = new Operation();

        operation.addImage(appProperties.getResources() + File.separator + AppProperties.SHARE_PATH);
        operation.addRawArgs("-pointsize", "70");
        operation.addRawArgs("-font", "Roboto-Bold");
        operation.addRawArgs("-gravity", "Center");
        operation.addRawArgs("-annotate", "0");
        operation.addRawArgs(magickText);

        Path resultPath = Paths.get(storageProperties.getRoot()).resolve(storageProperties.getShareRoot());

        File file = File.createTempFile("share.", ".png", resultPath.toFile());

        operation.addImage(file.getAbsolutePath());

        convertCmd.run(operation);

        return file.getName();
    }
}
