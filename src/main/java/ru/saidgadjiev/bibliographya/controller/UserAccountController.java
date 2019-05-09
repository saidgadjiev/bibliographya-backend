package ru.saidgadjiev.bibliographya.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.UserProfile;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;

import java.util.TimeZone;

/**
 * Created by said on 02.01.2019.
 */
@RestController
@RequestMapping("/api/user-accounts")
public class UserAccountController {

    private final BibliographyaUserDetailsService userAccountDetailsService;

    private BibliographyaMapper bibliographyaMapper;

    public UserAccountController(BibliographyaUserDetailsService userAccountDetailsService,
                                 BibliographyaMapper bibliographyaMapper) {
        this.userAccountDetailsService = userAccountDetailsService;
        this.bibliographyaMapper = bibliographyaMapper;
    }

    @GetMapping("/{id:[\\d]+}")
    public ResponseEntity<?> getProfile(TimeZone timeZone, @PathVariable("id") int userId) {
        UserProfile userAccount = userAccountDetailsService.getProfile(timeZone, userId);

        if (userAccount.getBiography() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(bibliographyaMapper.convertToAccountResponse(userAccount));
    }
}
