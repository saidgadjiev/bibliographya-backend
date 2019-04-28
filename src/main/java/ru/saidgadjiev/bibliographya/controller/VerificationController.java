package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.domain.SendVerificationResult;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;
import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.service.impl.VerificationServiceWrapper;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by said on 11.02.2019.
 */
@RestController
@RequestMapping("/api/verifications")
public class VerificationController {
    
    private final VerificationServiceWrapper verificationService;

    private ObjectMapper objectMapper;

    public VerificationController(VerificationServiceWrapper verificationService,
                                  ObjectMapper objectMapper) {
        this.verificationService = verificationService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(HttpServletRequest request,
                                    AuthenticationKey authenticationKey,
                                    @RequestParam("code") Integer code) {
        VerificationResult verificationResult = verificationService.verify(request, authenticationKey, code, false);
        
        if (verificationResult.isExpired()) {
            return ResponseEntity.status(498).build();
        }
        
        if (verificationResult.isInvalid()) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        }
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/resend")
    public ResponseEntity<?> resend(HttpServletRequest request,
                                    Locale locale,
                                    AuthenticationKey authenticationKey
    ) throws MessagingException {
        SendVerificationResult sendVerificationResult = verificationService.sendVerification(request, locale, authenticationKey);

        return ResponseEntity.status(sendVerificationResult.getStatus()).body(sendVerificationResult.getTimer());
    }
}
