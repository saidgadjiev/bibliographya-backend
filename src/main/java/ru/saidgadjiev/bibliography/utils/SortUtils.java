package ru.saidgadjiev.bibliography.utils;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;

/**
 * Created by said on 04.01.2019.
 */
public class SortUtils {

    private SortUtils() {

    }

    public static String toSql(Sort sort, String alias) {
        if (sort == null) {
            return null;
        }

        StringBuilder sortBuilder = new StringBuilder();

        for (Sort.Order order: sort) {
            if (sortBuilder.length() > 0) {
                sortBuilder.append(",");
            }

            String property = order.getProperty();

            if (StringUtils.isNotBlank(alias)) {
                property = alias + '.' + property;
            }

            sortBuilder.append(property).append(' ').append(order.getDirection().equals(Sort.Direction.ASC) ? "ASC" : "DESC");
        }

        return sortBuilder.toString();
    }
}
