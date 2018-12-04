package ru.saidgadjiev.bibliography.data;

/**
 * Created by said on 24.11.2018.
 */
public enum FilterOperation {

    EQ;

    public static FilterOperation from(String value) {
        if (value.compareToIgnoreCase("eq") == 0) {
            return EQ;
        }

        throw new UnsupportedOperationException();
    }
}
