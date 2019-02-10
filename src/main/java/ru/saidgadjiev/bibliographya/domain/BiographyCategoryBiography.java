package ru.saidgadjiev.bibliographya.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 09.02.2019.
 */
public class BiographyCategoryBiography {

    private int biographyId;

    private List<BiographyCategory> categories = new ArrayList<>();

    public int getBiographyId() {
        return biographyId;
    }

    public void setBiographyId(int biographyId) {
        this.biographyId = biographyId;
    }

    public List<BiographyCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<BiographyCategory> categories) {
        this.categories = categories;
    }
}
