package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.bussiness.moderation.ModerationAction;

import java.util.Collection;

/**
 * Created by said on 22.10.2018.
 */
public class MyBiographyResponse extends BiographyBaseResponse {

    private String moderationInfo;

    private Collection<ModerationAction> actions;

    public Collection<ModerationAction> getActions() {
        return actions;
    }

    public void setActions(Collection<ModerationAction> actions) {
        this.actions = actions;
    }

    public String getModerationInfo() {
        return moderationInfo;
    }

    public void setModerationInfo(String moderationInfo) {
        this.moderationInfo = moderationInfo;
    }
}
