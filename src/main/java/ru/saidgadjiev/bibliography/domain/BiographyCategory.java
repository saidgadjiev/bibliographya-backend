package ru.saidgadjiev.bibliography.domain;

/**
 * Created by said on 27.11.2018.
 */
public class BiographyCategory {

    private int id;

    private String name;

    private String imagePath;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
