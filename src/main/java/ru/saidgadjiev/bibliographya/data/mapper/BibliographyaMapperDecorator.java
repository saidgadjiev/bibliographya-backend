package ru.saidgadjiev.bibliographya.data.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;
import ru.saidgadjiev.bibliographya.model.BiographyFixResponse;
import ru.saidgadjiev.bibliographya.model.BiographyModerationResponse;
import ru.saidgadjiev.bibliographya.model.MyBiographyResponse;
import ru.saidgadjiev.bibliographya.service.impl.BiographyFixService;
import ru.saidgadjiev.bibliographya.service.impl.BiographyModerationService;

public abstract class BibliographyaMapperDecorator implements BibliographyaMapper {

    private BibliographyaMapper delegate;

    private BiographyFixService fixService;

    private BiographyModerationService biographyModerationService;

    @Override
    public BiographyModerationResponse convertToBiographyModerationResponse(Biography biography) {
        BiographyModerationResponse moderationResponse = delegate.convertToBiographyModerationResponse(biography);

        moderationResponse.setActions(biographyModerationService.getActions(biography));

        return moderationResponse;
    }

    @Override
    public MyBiographyResponse convertToMyBiographyResponse(Biography biography) {
        MyBiographyResponse myBiographyResponse = delegate.convertToMyBiographyResponse(biography);

        myBiographyResponse.setActions(biographyModerationService.getUserActions(biography));

        return myBiographyResponse;
    }

    @Override
    public BiographyFixResponse convertToBiographyFixResponse(BiographyFix biographyFix) {
        BiographyFixResponse fixResponse = delegate.convertToBiographyFixResponse(biographyFix);

        fixResponse.setActions(fixService.getActions(biographyFix));

        return fixResponse;
    }

    @Autowired
    public void setDelegate(BibliographyaMapper delegate) {
        this.delegate = delegate;
    }

    @Autowired
    public void setBiographyModerationService(BiographyModerationService biographyModerationService) {
        this.biographyModerationService = biographyModerationService;
    }

    @Autowired
    public void setFixService(BiographyFixService fixService) {
        this.fixService = fixService;
    }
}
