package ru.saidgadjiev.bibliography.dao.query.biography;

/**
 * Created by said on 31.12.2018.
 */
public class BaseFieldsHandler extends FieldHandler {

    @Override
    protected void doAppendSelectList(StringBuilder selectList) {
        selectList.append("b.first_name,");
        selectList.append("b.last_name,");
        selectList.append("b.middle_name,");
        selectList.append("b.id,");
        selectList.append("b.creator_id,");
        selectList.append("b.user_id,");
        selectList.append("b.updated_at,");
        selectList.append("b.moderation_status,");
        selectList.append("b.moderated_at,");
        selectList.append("b.moderator_id,");
        selectList.append("b.moderation_info,");
        selectList.append("b.biography");
    }

    @Override
    protected void doAppendFromClause(StringBuilder fromClause) {
        fromClause.append("biography b");
    }
}
