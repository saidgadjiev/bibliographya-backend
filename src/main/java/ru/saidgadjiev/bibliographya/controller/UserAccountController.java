package ru.saidgadjiev.bibliographya.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.saidgadjiev.bibliographya.data.mapper.BibliographyaMapper;
import ru.saidgadjiev.bibliographya.domain.AuthenticationKey;
import ru.saidgadjiev.bibliographya.domain.UserAccount;
import ru.saidgadjiev.bibliographya.service.api.BibliographyaUserDetailsService;

import java.sql.SQLException;
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

    @RequestMapping(value = "", method = RequestMethod.HEAD)
    public ResponseEntity checkPhone(AuthenticationKey authenticationKey) throws SQLException {
        if (userAccountDetailsService.isExist(authenticationKey)) {
            return ResponseEntity.status(HttpStatus.FOUND).build();
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id:[\\d]+}")
    public ResponseEntity<?> getAccount(TimeZone timeZone, @PathVariable("id") int userId) {
        UserAccount userAccount = userAccountDetailsService.getAccount(timeZone, userId);

        if (userAccount.getBiography() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(bibliographyaMapper.convertToAccountResponse(userAccount));
    }
}
