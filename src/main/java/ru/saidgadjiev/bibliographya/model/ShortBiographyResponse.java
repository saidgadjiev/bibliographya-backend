package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.bussiness.moderation.ModerationAction;

import java.util.Collection;

/**
 * Created by said on 30.01.2019.
 */
public class ShortBiographyResponse {

    private int id;

    private String firstName;

    private String lastName;

    private String middleName;

    private Collection<ModerationAction> actions;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Collection<ModerationAction> getActions() {
        return actions;
    }

    public void setActions(Collection<ModerationAction> actions) {
        this.actions = actions;
    }
}
