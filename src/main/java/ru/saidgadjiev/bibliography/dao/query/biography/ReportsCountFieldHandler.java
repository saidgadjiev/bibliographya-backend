package ru.saidgadjiev.bibliography.dao.query.biography;

/**
 * Created by said on 31.12.2018.
 */
public class ReportsCountFieldHandler extends FieldHandler {

    @Override
    protected void doAppendSelectList(StringBuilder selectList) {
        selectList.append("COUNT(*) as reportsCount");
    }

    @Override
    protected void doAppendFromClause(StringBuilder fromClause) {
        fromClause.append(" LEFT JOIN biography_report br ON br.biography_id = b.id ");
    }

    @Override
    protected void doAppendGroupBy(StringBuilder groupBy) {
        groupBy.append(" b.id ");
    }
}
