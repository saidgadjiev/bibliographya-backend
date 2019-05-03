package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.bussiness.fix.FixAction;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by said on 15.12.2018.
 */
public class BiographyFixResponse {

    public static final String FIXER_ID = "fixerId";

    public static final String STATUS = "status";

    private Integer id;

    private Integer status;

    private BiographyResponse biography;

    private ShortBiographyResponse fixer;

    private ShortBiographyResponse creator;

    private String fixText;

    private Collection<FixAction> actions = new ArrayList<>();

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

    public ShortBiographyResponse getFixer() {
        return fixer;
    }

    public void setFixer(ShortBiographyResponse fixer) {
        this.fixer = fixer;
    }

    public ShortBiographyResponse getCreator() {
        return creator;
    }

    public void setCreator(ShortBiographyResponse creator) {
        this.creator = creator;
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

    public String getFixText() {
        return fixText;
    }

    public void setFixText(String fixText) {
        this.fixText = fixText;
    }
}
