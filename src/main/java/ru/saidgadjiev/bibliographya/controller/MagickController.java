package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.service.impl.MagickService;

/**
 * Created by said on 12/04/2019.
 */
@RestController
@RequestMapping("/api/magick")
public class MagickController {

    private final ObjectMapper objectMapper;

    private final MagickService magickService;

    @Autowired
    public MagickController(ObjectMapper objectMapper, MagickService magickService) {
        this.objectMapper = objectMapper;
        this.magickService = magickService;
    }

    @GetMapping("")
    public ResponseEntity<?> magickShare(@RequestParam("magickText") String magickText,
                                         @RequestParam(value = "magickSize", required = false) String magickSize) throws Exception {
        String shareImg = magickService.createShareImg(magickText, magickSize);

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("path", shareImg);

        return ResponseEntity.ok(objectNode);
    }
}
