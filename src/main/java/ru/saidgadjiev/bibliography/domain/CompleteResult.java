package ru.saidgadjiev.bibliography.domain;

import ru.saidgadjiev.bibliography.domain.Biography;
import ru.saidgadjiev.bibliography.model.ModerationStatus;
import ru.saidgadjiev.bibliography.service.impl.moderation.handler.ModerationAction;

import java.util.Collection;

/**
 * Created by said on 17.12.2018.
 */
public class CompleteResult {

    private int updated;

    private Biography biography;

    private Collection<ModerationAction> actions;

    public CompleteResult(int updated, Biography biography, Collection<ModerationAction> actions) {
        this.updated = updated;
        this.biography = biography;
        this.actions = actions;
    }

    public CompleteResult(int updated) {
        this.updated = updated;
    }

    public int getUpdated() {
        return updated;
    }

    public Biography getBiography() {
        return biography;
    }

    public Collection<ModerationAction> getActions() {
        return actions;
    }
}
