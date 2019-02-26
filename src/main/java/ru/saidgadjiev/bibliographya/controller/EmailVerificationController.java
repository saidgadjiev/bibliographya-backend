package ru.saidgadjiev.bibliographya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.domain.EmailVerificationResult;
import ru.saidgadjiev.bibliographya.service.impl.SessionEmailVerificationService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by said on 11.02.2019.
 */
@RestController
@RequestMapping("/api/emails")
public class EmailVerificationController {
    
    private final SessionEmailVerificationService verificationService;
    
    public EmailVerificationController(SessionEmailVerificationService verificationService) {
        this.verificationService = verificationService;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(HttpServletRequest request, @RequestParam("email") String email, @RequestParam("code") Integer code) {
        EmailVerificationResult verificationResult = verificationService.verify(request, email, code);
        
        if (verificationResult.isExpired()) {
            return ResponseEntity.status(498).build();
        }
        
        if (verificationResult.isInvalid()) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/resend")
    public ResponseEntity<?> resend(HttpServletRequest request, @RequestParam("email") String email) {
        verificationService.sendVerification(request, email);

        return ResponseEntity.ok().build();
    }
}
