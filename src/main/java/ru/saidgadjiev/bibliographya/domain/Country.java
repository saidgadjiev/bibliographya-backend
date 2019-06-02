package ru.saidgadjiev.bibliographya.domain;

public class Country {

    public static final String TYPE = "country";

    public static final String ID = "id";

    public static final String NAME = "name";

    private int id;

    private String name;

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
}
