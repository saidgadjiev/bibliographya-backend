package ru.saidgadjiev.bibliographya.component;

import org.springframework.stereotype.Component;
import ru.saidgadjiev.bibliographya.service.impl.BiographyService;

/**
 * Created by said on 23.01.2019.
 */
@Component("biography")
public class BiographyComponent {

    private final BiographyService biographyService;

    public BiographyComponent(BiographyService biographyService) {
        this.biographyService = biographyService;
    }

    public boolean isIAuthor(int biographyId) {
        return biographyService.isIAuthor(biographyId);
    }
}
