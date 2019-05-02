package ru.saidgadjiev.bibliographya.data.operator;

import ru.saidgadjiev.bibliographya.data.FilterCriteria;
import ru.saidgadjiev.bibliographya.data.FilterOperation;

import java.util.Collection;
import java.util.Iterator;

public class Similar implements Operator {

    private boolean patternOnStart;

    private boolean patternOnEnd;

    public Similar(boolean patternOnStart, boolean patternOnEnd) {
        this.patternOnStart = patternOnStart;
        this.patternOnEnd = patternOnEnd;
    }

    @Override
    public String getClause(FilterCriteria criteria) {
        Collection<String> values = (Collection<String>) criteria.getFilterValue();
        StringBuilder clause = new StringBuilder("SIMILAR '");

        if (patternOnStart) {
            clause.append("%(");
        } else {
            clause.append("(");
        }

        for (Iterator<String> iterator = values.iterator(); iterator.hasNext();) {
            clause.append(iterator.next());

            if (iterator.hasNext()) {
                clause.append("|");
            }
        }

        if (patternOnEnd) {
            clause.append(")%");
        } else {
            clause.append(")");
        }

        clause.append("'");

        return clause.toString();
    }

    @Override
    public FilterOperation getType() {
        return FilterOperation.SIMILAR;
    }
}
