package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.properties.StorageProperties;
import ru.saidgadjiev.bibliographya.service.api.StorageService;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/upload")
@RestController
@Profile("dev")
public class UploadController {

    private StorageService storageService;

    private StorageProperties storageProperties;

    @Autowired
    public UploadController(StorageService storageService, StorageProperties storageProperties) {
        this.storageService = storageService;
        this.storageProperties = storageProperties;
    }

    @GetMapping("/**")
    public Resource serve(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return storageService.loadAsResource(uri.substring(storageProperties.getRoot().length() + 2));
    }
}
