package ru.saidgadjiev.bibliographya.domain;

/**
 * Created by said on 27.11.2018.
 */
public class BiographyCategory {

    public static final String TABLE = "biography_category";

    public static final String ID = "id";

    public static final String NAME = "name";

    public static final String IMAGE_PATH = "image_path";

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
