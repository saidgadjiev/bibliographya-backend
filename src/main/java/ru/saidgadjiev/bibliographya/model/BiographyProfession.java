package ru.saidgadjiev.bibliographya.model;

import ru.saidgadjiev.bibliographya.domain.Profession;

import java.util.ArrayList;
import java.util.List;

public class BiographyProfession {

    private int biographyId;

    private List<Profession> professions = new ArrayList<>();

    public int getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(int biographyId) {
        this.biographyId = biographyId;
    }

    public List<Profession> getProfessions() {
        return professions;
    }

    public void setProfessions(List<Profession> professions) {
        this.professions = professions;
    }
}
