package ru.saidgadjiev.bibliographya.service.api;

import ru.saidgadjiev.bibliographya.auth.social.AccessGrant;
import ru.saidgadjiev.bibliographya.auth.social.SocialUserInfo;

public interface SocialService {

    String createOAuth2Url(String redirectUri);

    AccessGrant createAccessToken(String code, String redirectUri);

    SocialUserInfo getUserInfo(String userId, String accessToken);
}
