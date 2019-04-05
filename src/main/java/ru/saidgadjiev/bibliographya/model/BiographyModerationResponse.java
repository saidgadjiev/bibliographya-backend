package ru.saidgadjiev.bibliographya.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.saidgadjiev.bibliographya.bussiness.moderation.ModerationAction;
import ru.saidgadjiev.bibliographya.configuration.BibliographyaConfiguration;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by said on 22.10.2018.
 */
public class BiographyModerationResponse extends BiographyBaseResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = BibliographyaConfiguration.DATE_FORMAT)
    private Timestamp moderatedAt;

    private Integer moderatorId;

    private ShortBiographyResponse moderator;

    private Collection<ModerationAction> actions = new ArrayList<>();

    private String moderationInfo;

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
