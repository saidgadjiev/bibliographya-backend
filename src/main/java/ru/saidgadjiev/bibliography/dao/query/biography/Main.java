package ru.saidgadjiev.bibliography.dao.query.biography;

/**
 * Created by said on 31.12.2018.
 */
public class Main {

    public static void main(String[] args) {
        FieldHandler baseFieldsHandler = new BaseFieldsHandler();

        baseFieldsHandler
                .setNext(new ModeratorBiographyFieldHandler())
                .setNext(new ReportsCountFieldHandler());

        StringBuilder selectList = new StringBuilder();
        StringBuilder fromClause = new StringBuilder();
        StringBuilder groupBy = new StringBuilder();

        baseFieldsHandler.appendFromClause(fromClause);
        baseFieldsHandler.appendSelectList(selectList);
        baseFieldsHandler.appendGroupBy(groupBy);

        String sql = "SELECT " + selectList.toString() + " FROM " + fromClause + " GROUP BY " + groupBy.toString();

        System.out.println(sql);
    }
}
