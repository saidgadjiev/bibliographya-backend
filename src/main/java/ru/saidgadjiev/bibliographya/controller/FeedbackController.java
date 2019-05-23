package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.domain.Feedback;
import ru.saidgadjiev.bibliographya.model.FeedbackRequest;
import ru.saidgadjiev.bibliographya.model.OffsetLimitPageRequest;
import ru.saidgadjiev.bibliographya.service.impl.FeedbackService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("")
    public ResponseEntity<?> create(@Valid @RequestBody FeedbackRequest feedbackRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().build();
        }

        feedbackService.create(feedbackRequest);

        return ResponseEntity.ok().build();
    }

    @GetMapping("")
    @PreAuthorize("hasAnyRole('ROLE_MODERATOR', 'ROLE_DEVELOPER')")
    public ResponseEntity<?> getList(OffsetLimitPageRequest pageRequest) {
        Page<Feedback> page = feedbackService.getList(pageRequest);

        if (page.getContent().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(page);
    }
}
