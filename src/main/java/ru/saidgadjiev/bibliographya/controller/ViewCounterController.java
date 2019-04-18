package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.service.impl.counter.ViewCounterService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/vc")
public class ViewCounterController {

    private ViewCounterService viewCounterService;

    @Autowired
    public ViewCounterController(ViewCounterService viewCounterService) {
        this.viewCounterService = viewCounterService;
    }

    @PostMapping("/{biographyId}")
    public ResponseEntity<?> view(HttpServletRequest request, @PathVariable("biographyId") int biographyId) {
        viewCounterService.hit(request, biographyId);

        return ResponseEntity.ok().build();
    }
}
