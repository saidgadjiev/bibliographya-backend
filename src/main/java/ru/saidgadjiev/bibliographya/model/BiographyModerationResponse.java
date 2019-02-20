package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.bussiness.moderation.ModerationAction;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by said on 22.10.2018.
 */
public class BiographyModerationResponse {

    private Integer moderationStatus;

    private Timestamp moderatedAt;

    private Integer moderatorId;

    private ShortBiographyResponse moderator;

    private Collection<ModerationAction> actions = new ArrayList<>();

    private String moderationInfo;

    public Integer getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(Integer moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public Timestamp getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(Timestamp moderatedAt) {
        this.moderatedAt = moderatedAt;
    }

    public Integer getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(Integer moderatorId) {
        this.moderatorId = moderatorId;
    }

    public ShortBiographyResponse getModerator() {
        return moderator;
    }

    public void setModerator(ShortBiographyResponse moderator) {
        this.moderator = moderator;
    }

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
