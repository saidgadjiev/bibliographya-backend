package ru.saidgadjiev.bibliography.data;

import java.sql.PreparedStatement;
import java.util.function.BiConsumer;

/**
 * Created by said on 24.11.2018.
 */
public class FilterCriteria {

    private String propertyName;

    private FilterOperation filterOperation;

    private PreparedSetter<Object> valueSetter;

    private Object filterValue;

    private boolean needPreparedSet = true;

    public FilterOperation getFilterOperation() {
        return filterOperation;
    }

    public void setFilterOperation(FilterOperation filterOperation) {
        this.filterOperation = filterOperation;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public PreparedSetter<Object> getValueSetter() {
        return valueSetter;
    }

    public void setValueSetter(PreparedSetter<Object> valueSetter) {
        this.valueSetter = valueSetter;
    }

    public Object getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(Object filterValue) {
        this.filterValue = filterValue;
    }

    public boolean isNeedPreparedSet() {
        return needPreparedSet;
    }

    public void setNeedPreparedSet(boolean needPreparedSet) {
        this.needPreparedSet = needPreparedSet;
    }
}
