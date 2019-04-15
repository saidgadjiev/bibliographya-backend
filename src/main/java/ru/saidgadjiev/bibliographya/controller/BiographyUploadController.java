package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

@RestController
@RequestMapping("api/biographies/media")
public class BiographyUploadController {

    private StorageService storageService;

    private ObjectMapper objectMapper;

    @Autowired
    public BiographyUploadController(@Qualifier("biography") StorageService storageService,
                                     ObjectMapper objectMapper) {
        this.storageService = storageService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("")
    public ResponseEntity<?> upload(@RequestPart("file") MultipartFile file) {
        storageService.store(file.getOriginalFilename(), file);

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("location", file.getOriginalFilename());

        return ResponseEntity.ok(objectNode);
    }

    @GetMapping("/{file}")
    public Resource serve(@PathVariable("file") String file) {
        return storageService.loadAsResource(file);
    }
}
