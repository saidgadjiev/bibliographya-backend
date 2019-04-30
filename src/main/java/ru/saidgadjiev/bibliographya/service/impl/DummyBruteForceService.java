package ru.saidgadjiev.bibliographya.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;
import ru.saidgadjiev.bibliographya.service.api.BruteForceService;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by said on 28/04/2019.
 */
@Service
@Profile(BibliographyaConfiguration.PROFILE_DEV)
public class DummyBruteForceService implements BruteForceService {

    @Override
    public void count(HttpServletRequest request, Type type) {

    }

    @Override
    public boolean isBlocked(HttpServletRequest request, Type type) {
        return false;
    }
}
