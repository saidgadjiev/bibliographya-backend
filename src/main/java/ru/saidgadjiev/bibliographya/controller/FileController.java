package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.saidgadjiev.bibliographya.service.api.StorageService;
import ru.saidgadjiev.bibliographya.service.impl.storage.StorageFileNotFoundException;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final StorageService storageService;

    @Autowired
    public FileController(@Qualifier("resourceStorageService") StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping(value = "/{filePath}")
    public ResponseEntity<Resource> serverFile(@PathVariable("filePath") String filePath) {
        Resource resource = storageService.loadAsResource(filePath);

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + resource.getFilename() + "\"").body(resource);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/category/image")
    public ResponseEntity<?> uploadCategoryImage(@RequestParam(value="file") MultipartFile file) {
        String path = storageService.storeCategoryImage(file);
        ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();

        jsonNode.put("path", path);

        return ResponseEntity.ok(jsonNode);
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
