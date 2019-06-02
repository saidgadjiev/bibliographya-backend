package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.domain.Profession;
import ru.saidgadjiev.bibliographya.service.impl.ProfessionService;

import java.util.List;

@RestController
@RequestMapping("/api/professions")
public class ProfessionController {

    private ProfessionService professionService;

    @Autowired
    public ProfessionController(ProfessionService professionService) {
        this.professionService = professionService;
    }

    @GetMapping("")
    public ResponseEntity<?> getCountries(
            @RequestParam(name = "query", required = false) String query
    ) {
        List<Profession> countries = professionService.getProfessions(query);

        return ResponseEntity.ok(countries);
    }
}
