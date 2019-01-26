package ru.saidgadjiev.bibliographya.data;

/**
 * Created by said on 17.12.2018.
 */
public class UpdateValue<T> {

    private String name;

    private T value;

    private boolean needPreparedSet = true;

    private PreparedSetter<T> setter;

    public UpdateValue() {
    }

    public UpdateValue(String name, T value, boolean needPreparedSet, PreparedSetter<T> setter) {
        this.name = name;
        this.value = value;
        this.needPreparedSet = needPreparedSet;
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

    public Object getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isNeedPreparedSet() {
        return needPreparedSet;
    }

    public void setNeedPreparedSet(boolean needPreparedSet) {
        this.needPreparedSet = needPreparedSet;
    }
}
