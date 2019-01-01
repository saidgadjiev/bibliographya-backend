package ru.saidgadjiev.bibliography.dao.query.biography;

import java.lang.reflect.Field;

/**
 * Created by said on 31.12.2018.
 */
public abstract class FieldHandler {

    private FieldHandler next;


    public FieldHandler setNext(FieldHandler next) {
        this.next = next;

        return next;
    }

    public void appendSelectList(StringBuilder selectList) {
        doAppendSelectList(selectList);

        if (next != null) {
            if (!selectList.toString().endsWith(",")) {
                selectList.append(",");
            }
            next.appendSelectList(selectList);
        }
    }

    public void appendFromClause(StringBuilder fromClause) {
        doAppendFromClause(fromClause);

        if (next != null) {
            next.appendFromClause(fromClause);
        }
    }

    public void appendGroupBy(StringBuilder groupBy) {
        doAppendGroupBy(groupBy);

        if (next != null) {
            next.appendGroupBy(groupBy);
        }
    }

    protected abstract void doAppendSelectList(StringBuilder selectList);

    protected abstract void doAppendFromClause(StringBuilder fromClause);

    protected void doAppendGroupBy(StringBuilder groupBy) {

    }

}
