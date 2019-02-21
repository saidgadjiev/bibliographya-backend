package ru.saidgadjiev.bibliographya.factory;

import org.springframework.stereotype.Service;
import ru.saidgadjiev.bibliographya.auth.common.ProviderType;
import ru.saidgadjiev.bibliographya.service.api.SocialService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.FacebookService;
import ru.saidgadjiev.bibliographya.service.impl.auth.social.VKService;

@Service
public class SocialServiceFactory {

    private FacebookService facebookService;

    private VKService vkService;

    public SocialServiceFactory(FacebookService facebookService, VKService vkService) {
        this.facebookService = facebookService;
        this.vkService = vkService;
    }

    public SocialService getService(ProviderType providerType) {
        switch (providerType) {
            case FACEBOOK:
                return facebookService;
            case VK:
                return vkService;
        }

        return null;
    }
}
