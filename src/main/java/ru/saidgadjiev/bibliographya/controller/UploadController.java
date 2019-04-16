package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.domain.Image;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.service.api.StorageService;
import ru.saidgadjiev.bibliographya.service.impl.ImageService;
import ru.saidgadjiev.bibliographya.service.impl.StashImageService;
import ru.saidgadjiev.bibliographya.utils.FileNameUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("api/media/")
public class UploadController {

    private StashImageService stashImageService;

    private StorageService storageService;

    private ObjectMapper objectMapper;

    @Autowired
    public UploadController(StorageService storageService,
                            ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    @PutMapping("")
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());

        String fileName = new SimpleDateFormat("upload_'yyyyMMddHHmmSSSSS'." + ext + "'").format(new Date());

        String filePath = StorageProperties.TEMP_ROOT + "/" + fileName;

        storageService.store(filePath, file);

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("location", filePath);

        stashImageService.create(fileName);

        return ResponseEntity.ok(objectNode);
    }

    @GetMapping("")
    public Resource serve(@RequestParam("filePath") String filePath) {
        return storageService.loadAsResource(filePath);
    }
}
