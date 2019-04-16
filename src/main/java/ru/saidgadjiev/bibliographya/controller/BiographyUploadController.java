package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.service.api.StorageService;
import ru.saidgadjiev.bibliographya.utils.FileNameUtils;

@RestController
@RequestMapping("api/media/biographies")
public class BiographyUploadController {

    private StorageService storageService;

    private ObjectMapper objectMapper;

    @Autowired
    public BiographyUploadController(StorageService storageService,
                                     ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    @PutMapping("")
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) {
        String filePath = FileNameUtils.biographyUploadPath(file);

        storageService.store(filePath, file);

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("location", filePath);

        return ResponseEntity.ok(objectNode);
    }

    @GetMapping("")
    public Resource serve(@RequestParam("filePath") String filePath) {
        return storageService.loadAsResource(filePath);
    }
}
