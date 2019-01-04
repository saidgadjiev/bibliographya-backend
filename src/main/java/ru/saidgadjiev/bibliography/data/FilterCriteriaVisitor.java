package ru.saidgadjiev.bibliography.data;

import cz.jirutka.rsql.parser.ast.*;

import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 04.01.2019.
 */
public class FilterCriteriaVisitor<R, A> implements RSQLVisitor<R, A> {

    private final Collection<FilterCriteria> criteria;

    private final Map<String, Type> fieldsTypes;

    public FilterCriteriaVisitor(Collection<FilterCriteria> criteria,
                                 Map<String, Type> fieldsTypes) {
        this.criteria = criteria;
        this.fieldsTypes = fieldsTypes;
    }

    @Override
    public R visit(AndNode andNode, A a) {
        andNode.getChildren().forEach(node -> node.accept(this));
        return null;
    }

    @Override
    public R visit(OrNode orNode, A a) {
        return null;
    }

    @Override
    public R visit(ComparisonNode comparisonNode, A a) {
        if (!fieldsTypes.containsKey(comparisonNode.getSelector())) {
            return null;
        }
        if (comparisonNode.getOperator().equals(RSQLOperators.EQUAL)) {
            List<String> arguments = comparisonNode.getArguments();

            if (arguments.isEmpty()) {
                return null;
            }
            String argument = arguments.iterator().next();

            switch (fieldsTypes.get(comparisonNode.getSelector())) {
                case INTEGER:
                    if (argument.equals("null")) {
                        FilterCriteria<String> criterion = new FilterCriteria<>(
                                comparisonNode.getSelector(),
                                FilterOperation.IS_NULL,
                                (preparedStatement, index, value) -> preparedStatement.setNull(index, Types.INTEGER),
                                null,
                                true
                        );

                        criteria.add(criterion);
                    } else {
                        FilterCriteria<Integer> criterion = new FilterCriteria<>(
                                comparisonNode.getSelector(),
                                FilterOperation.EQ,
                                PreparedStatement::setInt,
                                Integer.parseInt(argument),
                                true
                        );

                        criteria.add(criterion);
                    }

                    break;
            }
        }

        return null;
    }

    public enum Type {

        INTEGER

    }
}
