package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.bussiness.fix.FixAction;

import java.util.Collection;

/**
 * Created by said on 15.12.2018.
 */
public class BiographyFixResponse {

    private Integer id;

    private Integer status;

    private BiographyResponse biography;

    private ShortBiographyResponse fixerBiography;

    private ShortBiographyResponse creatorBiography;

    private Collection<FixAction> actions;

    private String info;

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

    public ShortBiographyResponse getFixerBiography() {
        return fixerBiography;
    }

    public void setFixerBiography(ShortBiographyResponse fixerBiography) {
        this.fixerBiography = fixerBiography;
    }

    public ShortBiographyResponse getCreatorBiography() {
        return creatorBiography;
    }

    public void setCreatorBiography(ShortBiographyResponse creatorBiography) {
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
