package ru.saidgadjiev.bibliography.dao.query.biography;

/**
 * Created by said on 31.12.2018.
 */
public class ModeratorBiographyFieldHandler extends FieldHandler {

    @Override
    protected void doAppendSelectList(StringBuilder selectList) {
        selectList.append("bm.first_name as m_first_name,");
        selectList.append("bm.last_name as m_last_name,");
        selectList.append("bm.id as m_id");
    }

    @Override
    protected void doAppendFromClause(StringBuilder fromClause) {
        fromClause.append(" LEFT JOIN biography bm ON b.moderator_id = bm.user_id ");
    }
}
