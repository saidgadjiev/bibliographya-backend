package ru.saidgadjiev.bibliographya.data.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;
import ru.saidgadjiev.bibliographya.model.BiographyFixResponse;
import ru.saidgadjiev.bibliographya.model.BiographyModerationResponse;
import ru.saidgadjiev.bibliographya.model.MyBiographyResponse;
import ru.saidgadjiev.bibliographya.service.impl.BiographyFixService;
import ru.saidgadjiev.bibliographya.service.impl.BiographyModerationService;

import java.util.ArrayList;
import java.util.List;

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
    public List<BiographyModerationResponse> convertToBiographyModerationResponse(List<Biography> biographies) {
        if (biographies == null) {
            return null;
        }

        List<BiographyModerationResponse> list = new ArrayList<>(biographies.size());

        for (Biography biography : biographies) {
            list.add(convertToBiographyModerationResponse(biography));
        }

        return list;
    }

    @Override
    public MyBiographyResponse convertToMyBiographyResponse(Biography biography) {
        MyBiographyResponse myBiographyResponse = delegate.convertToMyBiographyResponse(biography);

        myBiographyResponse.setActions(biographyModerationService.getUserActions(biography));

        return myBiographyResponse;
    }


    @Override
    public List<MyBiographyResponse> convertToMyBiographyResponse(List<Biography> biographies) {
        if (biographies == null) {
            return null;
        }

        List<MyBiographyResponse> list = new ArrayList<>(biographies.size());

        for (Biography biography : biographies) {
            list.add(convertToMyBiographyResponse(biography));
        }

        return list;
    }

    @Override
    public BiographyFixResponse convertToBiographyFixResponse(BiographyFix biographyFix) {
        BiographyFixResponse fixResponse = delegate.convertToBiographyFixResponse(biographyFix);

        fixResponse.setActions(fixService.getActions(biographyFix));

        return fixResponse;
    }

    @Override
    public List<BiographyFixResponse> convertToBiographyFixResponse(List<BiographyFix> biographyFixes) {
        if (biographyFixes == null) {
            return null;
        }

        List<BiographyFixResponse> list = new ArrayList<>(biographyFixes.size());

        for (BiographyFix biographyFix : biographyFixes) {
            list.add(convertToBiographyFixResponse(biographyFix));
        }

        return list;
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
