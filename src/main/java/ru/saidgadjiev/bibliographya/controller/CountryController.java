package ru.saidgadjiev.bibliographya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.saidgadjiev.bibliographya.domain.Country;
import ru.saidgadjiev.bibliographya.service.impl.CountryService;

import java.util.List;

@RestController
@RequestMapping("/api/countries")
public class CountryController {

    private CountryService countryService;

    @Autowired
    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    @GetMapping("")
    public ResponseEntity<?> getCountries(
            @RequestParam(name = "query", required = false) String query
    ) {
        List<Country> countries = countryService.getCountries(query);

        return ResponseEntity.ok(countries);
    }
}
