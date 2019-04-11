package ru.saidgadjiev.bibliographya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by said on 12/04/2019.
 */
@RestController
@RequestMapping("/api/magick")
public class MagickController {

    @PutMapping("")
    public ResponseEntity<?> magickShare() {

    }
}
