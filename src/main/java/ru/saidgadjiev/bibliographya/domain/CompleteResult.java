package ru.saidgadjiev.bibliographya.domain;

import java.util.Collection;

/**
 * Created by said on 17.12.2018.
 */
public class CompleteResult<O, A> {

    private int updated;

    private O biography;

    private Collection<A> actions;

    public CompleteResult(int updated, O biography, Collection<A> actions) {
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

    public O getObject() {
        return biography;
    }

    public Collection<A> getActions() {
        return actions;
    }
}
