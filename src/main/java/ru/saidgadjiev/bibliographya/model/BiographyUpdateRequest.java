package ru.saidgadjiev.bibliographya.model;

import java.util.Collection;

public class BiographyUpdateRequest {

    private Boolean anonymousCreator;

    private Boolean disableComments;

    private Collection<String> returnFields;

    public Boolean getAnonymousCreator() {
        return anonymousCreator;
    }

    public void setAnonymousCreator(Boolean anonymousCreator) {
        this.anonymousCreator = anonymousCreator;
    }

    public Boolean getDisableComments() {
        return disableComments;
    }

    public void setDisableComments(Boolean disableComments) {
        this.disableComments = disableComments;
    }

    public Collection<String> getReturnFields() {
        return returnFields;
    }

    public void setReturnFields(Collection<String> returnFields) {
        this.returnFields = returnFields;
    }
}
