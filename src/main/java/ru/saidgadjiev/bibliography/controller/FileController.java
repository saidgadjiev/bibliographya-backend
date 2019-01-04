package ru.saidgadjiev.bibliography.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliography.service.api.StorageService;
import ru.saidgadjiev.bibliography.service.impl.storage.StorageFileNotFoundException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final StorageService storageService;

    @Autowired
    public FileController(@Qualifier("resourceStorageService") StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping(value = "/{filename}")
    public ResponseEntity<Resource> serverFile(@PathVariable("filename") String filename) {
        Resource resource = storageService.loadAsResource(filename);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
