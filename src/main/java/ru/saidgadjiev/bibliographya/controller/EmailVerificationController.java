package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.domain.SentVerification;
import ru.saidgadjiev.bibliographya.service.impl.EmailVerificationService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by said on 11.02.2019.
 */
@RestController
@RequestMapping("/api/emails")
public class EmailVerificationController {
    
    private final EmailVerificationService verificationService;

    private ObjectMapper objectMapper;

    public EmailVerificationController(EmailVerificationService verificationService, ObjectMapper objectMapper) {
        this.verificationService = verificationService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(HttpServletRequest request, @RequestParam("email") String email, @RequestParam("code") Integer code) {
        VerificationResult verificationResult = verificationService.verify(request, email, code);
        
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
                                    @RequestParam("email") String email
    ) throws MessagingException {
        SentVerification sentVerification = verificationService.sendVerification(request, locale, email);

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("tjwt", sentVerification.getTjwt());

        return ResponseEntity.status(sentVerification.getStatus()).body(objectNode);
    }
}
