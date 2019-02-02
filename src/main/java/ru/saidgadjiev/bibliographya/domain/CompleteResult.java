package ru.saidgadjiev.bibliographya.domain;

import java.util.Collection;

/**
 * Created by said on 17.12.2018.
 */
public class CompleteResult<O, A> {

    private int updated;

    private O biography;

    public CompleteResult(int updated, O biography) {
        this.updated = updated;
        this.biography = biography;
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
}
