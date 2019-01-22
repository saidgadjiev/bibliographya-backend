package ru.saidgadjiev.bibliography.data;

/**
 * Created by said on 24.11.2018.
 */
public enum FilterOperation {

    EQ ("eq"),
    FIELDS_EQ("fields_eq"),
    FIELDS_NOT_EQ("fields_not_eq"),
    NOT_EQ("not_eq"),
    IS_NOT_NULL("is_not_null"),
    IS_NULL ("is_null");

    private final String desc;

    FilterOperation(String desc) {
        this.desc = desc;
    }

    public static FilterOperation from(String value) {
        for (FilterOperation operation: values()) {
            if (operation.desc.equals(value)) {
                return operation;
            }
        }

        throw new UnsupportedOperationException();
    }
}
