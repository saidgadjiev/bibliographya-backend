package ru.saidgadjiev.bibliographya.utils;

import ru.saidgadjiev.bibliographya.data.FilterCriteria;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by said on 17.12.2018.
 */
public class FilterUtils {

    private FilterUtils() {

    }

    public static String toClause(Collection<FilterCriteria> criteria, String alias) {
        if (criteria == null) {
            return null;
        }
        StringBuilder clause = new StringBuilder();

        if (!criteria.isEmpty()) {
            for (Iterator<FilterCriteria> iterator = criteria.iterator(); iterator.hasNext(); ) {
                FilterCriteria criterion = iterator.next();

                switch (criterion.getFilterOperation().getType()) {
                    case EQ:
                        if (alias != null) {
                            clause.append(alias).append(".").append(criterion.getPropertyName()).append("=").append("?");
                        } else {
                            clause.append(criterion.getPropertyName()).append("=").append("?");
                        }

                        break;
                    case FIELDS_EQ:
                        if (alias != null) {
                            clause.append(alias).append(".").append(criterion.getPropertyName()).append("=").append(alias).append(".").append(criterion.getFilterValue());
                        } else {
                            clause.append(criterion.getPropertyName()).append("=").append(criterion.getFilterValue());
                        }

                        break;
                    case FIELDS_NOT_EQ:
                        if (alias != null) {
                            clause.append(alias).append(".").append(criterion.getPropertyName()).append("<>").append(alias).append(".").append(criterion.getFilterValue());
                        } else {
                            clause.append(criterion.getPropertyName()).append("<>").append(criterion.getFilterValue());
                        }

                        break;
                    case NOT_EQ:
                        if (alias != null) {
                            clause.append(alias).append(".").append(criterion.getPropertyName()).append("<>").append("?");
                        } else {
                            clause.append(criterion.getPropertyName()).append("<>").append("?");
                        }

                        break;
                    case IS_NULL:
                        if (alias != null) {
                            clause.append(alias).append(".").append(criterion.getPropertyName()).append(" IS NULL");
                        } else {
                            clause.append(criterion.getPropertyName()).append(" IS NULL");
                        }
                        break;
                    case LIKE:
                        if (alias != null) {
                            clause
                                    .append(alias).append(".").append(criterion.getPropertyName())
                                    .append(criterion.getFilterOperation().getClause(null));
                        } else {
                            clause
                                    .append(criterion.getPropertyName())
                                    .append(criterion.getFilterOperation().getClause(null));
                        }
                        break;
                    case IS_NOT_NULL:
                        if (alias != null) {
                            clause.append(alias).append(".").append(criterion.getPropertyName()).append(" IS NOT NULL");
                        } else {
                            clause.append(criterion.getPropertyName()).append(" IS NOT NULL");
                        }
                        break;
                    case SIMILAR:
                        if (alias != null) {
                            clause
                                    .append(alias).append(".").append(criterion.getPropertyName()).append(" ")
                                    .append(criterion.getFilterOperation().getClause(criterion));
                        } else {
                            clause
                                    .append(criterion.getPropertyName())
                                    .append(" ")
                                    .append(criterion.getFilterOperation().getClause(criterion));
                        }
                        break;
                }
                if (iterator.hasNext()) {
                    switch (criterion.getLogicOperator()) {
                        case OR:
                            clause.append(" OR ");
                            break;
                        case AND:
                            clause.append(" AND ");
                            break;
                    }
                }
            }
        }

        return clause.toString();
    }
}
