package ru.saidgadjiev.bibliographya.data.operator;

import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;

public interface Operator {

    String getClause(FilterCriteria criteria);

    FilterOperation getType();
}
