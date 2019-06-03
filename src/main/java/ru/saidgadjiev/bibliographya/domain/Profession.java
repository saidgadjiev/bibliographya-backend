package ru.saidgadjiev.bibliographya.domain;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Profession implements Comparable<Profession> {

    public static final String TYPE = "profession";

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

    @Override
    public int compareTo(@NotNull Profession o) {
        return Objects.compare(name, o.name, String::compareTo);
    }
}
