package ru.saidgadjiev.bibliography.model;

import ru.saidgadjiev.bibliography.bussiness.fix.FixAction;

import java.util.Collection;

/**
 * Created by said on 15.12.2018.
 */
public class BiographyFixResponse {

    private Integer id;

    private Integer status;

    private BiographyResponse biography;

    private BiographyResponse fixerBiography;

    private BiographyResponse creatorBiography;

    private Collection<FixAction> actions;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BiographyResponse getBiography() {
        return biography;
    }

    public void setBiography(BiographyResponse biography) {
        this.biography = biography;
    }

    public BiographyResponse getFixerBiography() {
        return fixerBiography;
    }

    public void setFixerBiography(BiographyResponse fixerBiography) {
        this.fixerBiography = fixerBiography;
    }

    public BiographyResponse getCreatorBiography() {
        return creatorBiography;
    }

    public void setCreatorBiography(BiographyResponse creatorBiography) {
        this.creatorBiography = creatorBiography;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Collection<FixAction> getActions() {
        return actions;
    }

    public void setActions(Collection<FixAction> actions) {
        this.actions = actions;
    }
}
