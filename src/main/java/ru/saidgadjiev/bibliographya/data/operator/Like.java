package ru.saidgadjiev.bibliographya.data.operator;

import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;

public class Like implements Operator {

    private boolean patternOnStart;

    private boolean patternOnEnd;

    public Like(boolean patternOnStart, boolean patternOnEnd) {
        this.patternOnStart = patternOnStart;
        this.patternOnEnd = patternOnEnd;
    }

    @Override
    public String getClause(FilterCriteria criteria) {
        StringBuilder clause = new StringBuilder("LIKE '");

        if (patternOnStart) {
            clause.append("%");
        }
        clause.append("?");

        if (patternOnEnd) {
            clause.append("%");
        }

        clause.append("'");

        return clause.toString();
    }

    @Override
    public FilterOperation getType() {
        return FilterOperation.LIKE;
    }
}
