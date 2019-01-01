package ru.saidgadjiev.bibliography.data;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by said on 17.12.2018.
 */
public class FilterUtils {

    private FilterUtils() {

    }

    public static String toClause(Collection<FilterCriteria> criteria, String alias) {
        StringBuilder clause = new StringBuilder();

        if (criteria != null && !criteria.isEmpty()) {
            for (Iterator<FilterCriteria> iterator = criteria.iterator(); iterator.hasNext(); ) {
                FilterCriteria criterion = iterator.next();

                switch (criterion.getFilterOperation()) {
                    case EQ:
                        if (alias != null) {
                            clause.append(alias).append(".").append(criterion.getPropertyName()).append("=").append("?");
                        } else {
                            clause.append(criterion.getPropertyName()).append("=").append("?");
                        }

                        break;
                    case IS_NULL:
                        if (alias != null) {
                            clause.append(alias).append(".").append(criterion.getPropertyName()).append(" IS NULL");
                        } else {
                            clause.append(criterion.getPropertyName()).append(" IS NULL");
                        }
                }
                if (iterator.hasNext()) {
                    clause.append(" AND ");
                }
            }
        }

        return clause.toString();
    }
}
