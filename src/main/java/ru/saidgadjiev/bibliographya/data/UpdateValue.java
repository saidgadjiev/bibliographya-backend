package ru.saidgadjiev.bibliographya.data;

/**
 * Created by said on 17.12.2018.
 */
public class UpdateValue<T> {

    private String name;

    private PreparedSetter<T> setter;

    public UpdateValue(String name, PreparedSetter<T> setter) {
        this.name = name;
        this.setter = setter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PreparedSetter<T> getSetter() {
        return setter;
    }

    public void setSetter(PreparedSetter<T> setter) {
        this.setter = setter;
    }

}
