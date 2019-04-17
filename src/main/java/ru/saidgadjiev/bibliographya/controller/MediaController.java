package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.service.api.StorageService;
import ru.saidgadjiev.bibliographya.service.impl.StashMediaService;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private StashMediaService stashMediaService;

    private StorageService storageService;

    private ObjectMapper objectMapper;

    @Autowired
    public MediaController(StashMediaService stashMediaService,
                           StorageService storageService,
                           ObjectMapper objectMapper) {
        this.stashMediaService = stashMediaService;
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    @PutMapping("")
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());

        String filePath = StorageProperties.TEMP_ROOT + "/" + new SimpleDateFormat("'upload_'yyyyMMddHHmmSSSSS'." + ext + "'").format(new Date());

        storageService.store(filePath, file);

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("location", filePath);

        stashMediaService.create(filePath);

        return ResponseEntity.ok(objectNode);
    }
}
