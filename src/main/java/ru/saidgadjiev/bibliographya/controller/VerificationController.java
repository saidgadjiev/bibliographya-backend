package ru.saidgadjiev.bibliographya.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.domain.SentVerification;
import ru.saidgadjiev.bibliographya.domain.VerificationResult;
import ru.saidgadjiev.bibliographya.service.api.VerificationService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Created by said on 11.02.2019.
 */
@RestController
@RequestMapping("/api/verifications")
public class VerificationController {
    
    private final VerificationService emailVerificationService;

    private final VerificationService phoneVerificationService;

    private ObjectMapper objectMapper;

    public VerificationController(VerificationService emailVerificationService,
                                  VerificationService phoneVerificationService,
                                  ObjectMapper objectMapper) {
        this.emailVerificationService = emailVerificationService;
        this.phoneVerificationService = phoneVerificationService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(HttpServletRequest request, @RequestParam("verificationKey") String key, @RequestParam("code") Integer code) {
        VerificationResult verificationResult = emailVerificationService.verify(request, key, code);
        
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
                                    @RequestParam("verificationKey") String verificationKey
    ) throws MessagingException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        SentVerification sentVerification = emailVerificationService.sendVerification(request, locale, verificationKey);

        ObjectNode objectNode = objectMapper.createObjectNode();

        objectNode.put("tjwt", sentVerification.getTjwt());

        return ResponseEntity.status(sentVerification.getStatus()).body(objectNode);
    }
}
