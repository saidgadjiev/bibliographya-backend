package ru.saidgadjiev.bibliographya.model;

import java.util.Map;

public class UpdateRequest {

    private Map<String, Object> values;

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }
}
