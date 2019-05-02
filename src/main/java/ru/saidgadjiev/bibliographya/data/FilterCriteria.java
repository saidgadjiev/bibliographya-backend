package ru.saidgadjiev.bibliographya.data;

import ru.saidgadjiev.bibliographya.data.operator.Operator;

import java.util.Objects;

/**
 * Created by said on 24.11.2018.
 */
public class FilterCriteria<T> {

    private String propertyName;

    private Operator filterOperation;

    private LogicOperator logicOperator = LogicOperator.AND;

    private PreparedSetter<T> valueSetter;

    private T filterValue;

    private boolean needPreparedSet = true;

    public FilterCriteria() {
    }

    public FilterCriteria(String propertyName,
                          Operator filterOperation,
                          PreparedSetter<T> valueSetter,
                          T filterValue,
                          boolean needPreparedSet) {
        this.propertyName = propertyName;
        this.filterOperation = filterOperation;
        this.valueSetter = valueSetter;
        this.filterValue = filterValue;
        this.needPreparedSet = needPreparedSet;
    }

    public FilterCriteria(String propertyName,
                          Operator filterOperation,
                          PreparedSetter<T> valueSetter,
                          T filterValue,
                          LogicOperator logicOperator,
                          boolean needPreparedSet) {
        this.propertyName = propertyName;
        this.filterOperation = filterOperation;
        this.valueSetter = valueSetter;
        this.filterValue = filterValue;
        this.logicOperator = logicOperator;
        this.needPreparedSet = needPreparedSet;
    }

    public Operator getFilterOperation() {
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

    public PreparedSetter<T> getValueSetter() {
        return valueSetter;
    }

    public void setValueSetter(PreparedSetter<T> valueSetter) {
        this.valueSetter = valueSetter;
    }

    public Object getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(T filterValue) {
        this.filterValue = filterValue;
    }

    public boolean isNeedPreparedSet() {
        return needPreparedSet;
    }

    public void setNeedPreparedSet(boolean needPreparedSet) {
        this.needPreparedSet = needPreparedSet;
    }

    public LogicOperator getLogicOperator() {
        return logicOperator;
    }

    public void setLogicOperator(LogicOperator logicOperator) {
        this.logicOperator = logicOperator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilterCriteria<?> that = (FilterCriteria<?>) o;
        return propertyName.equals(that.propertyName) &&
                filterOperation == that.filterOperation &&
                Objects.equals(filterValue, that.filterValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyName, filterOperation, filterValue);
    }

    public static final class Builder<T> {

        private String propertyName;

        private Operator filterOperation;

        private LogicOperator logicOperator = LogicOperator.AND;

        private PreparedSetter<T> valueSetter;

        private T filterValue;

        private boolean needPreparedSet = true;

        public Builder<T> propertyName(String propertyName) {
            this.propertyName = propertyName;

            return this;
        }

        public Builder<T> filterOperation(Operator filterOperation) {
            this.filterOperation = filterOperation;

            return this;
        }

        public Builder<T> logicOperator(LogicOperator logicOperator) {
            this.logicOperator = logicOperator;

            return this;
        }

        public Builder<T> valueSetter(PreparedSetter<T> valueSetter) {
            this.valueSetter = valueSetter;

            return this;
        }

        public Builder<T> filterValue(T filterValue) {
            this.filterValue = filterValue;

            return this;
        }

        public Builder<T> needPreparedSet(boolean needPreparedSet) {
            this.needPreparedSet = needPreparedSet;

            return this;
        }

        public FilterCriteria<T> build() {
            return new FilterCriteria<>(
                    propertyName,
                    filterOperation,
                    valueSetter,
                    filterValue,
                    logicOperator,
                    needPreparedSet
            );
        }
    }
}
