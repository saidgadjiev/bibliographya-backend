package ru.saidgadjiev.bibliographya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;
import ru.saidgadjiev.bibliographya.service.impl.EmailVerificationService;

/**
 * Created by said on 11.02.2019.
 */
@RestController
@RequestMapping("/api/emails")
public class EmailVerificationController {
    
    private final EmailVerificationService verificationService;
    
    public EmailVerificationController(EmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestParam("email") String email, @RequestParam("code") Integer code) {
        EmailVerificationResult verificationResult = verificationService.verify(email, code);
        
        if (verificationResult.isExpired()) {
            return ResponseEntity.status(498).build();
        }
        
        if (verificationResult.isInvalid()) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/resend")
    public ResponseEntity<?> resend(@RequestParam("email") String email) {
        int resend = verificationService.resend(email);
        
        if (resend == 0) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }
}
