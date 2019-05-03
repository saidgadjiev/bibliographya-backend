package ru.saidgadjiev.bibliographya.data;

import cz.jirutka.rsql.parser.ast.*;
import ru.saidgadjiev.bibliographya.data.mapper.FieldsMapper;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.AndCondition;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.Equals;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.IsNull;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by said on 04.01.2019.
 */
public class ClientQueryVisitor<R, A> implements RSQLVisitor<R, A> {

    private final AndCondition condition = new AndCondition();

    private final List<PreparedSetter> preparedSetters = new ArrayList<>();

    private FieldsMapper fieldsMapper;

    public ClientQueryVisitor(FieldsMapper fieldsMapper) {
        this.fieldsMapper = fieldsMapper;
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
        if (!fieldsMapper.has(comparisonNode.getSelector())) {
            return null;
        }
        if (comparisonNode.getOperator().equals(RSQLOperators.EQUAL)) {
            List<String> arguments = comparisonNode.getArguments();

            if (arguments.isEmpty()) {
                return null;
            }
            String field = fieldsMapper.getField(comparisonNode.getSelector());
            String argument = arguments.iterator().next();

            if (argument.equals("null")) {
                condition.add(new IsNull(new ColumnSpec(comparisonNode.getSelector())));
            } else {
                switch (fieldsMapper.getType(comparisonNode.getSelector())) {
                    case INTEGER: {
                        condition.add(new Equals(new ColumnSpec(field), new Param()));
                        preparedSetters.add((preparedStatement, index) -> {
                            preparedStatement.setInt(index, Integer.parseInt(argument));
                        });

                        break;
                    }
                    case STRING: {
                        condition.add(new Equals(new ColumnSpec(field), new Param()));
                        preparedSetters.add((preparedStatement, index) -> {
                            preparedStatement.setString(index, argument);
                        });

                        break;
                    }
                }
            }
        }

        return null;
    }

    public AndCondition getCondition() {
        return condition;
    }

    public List<PreparedSetter> getValues() {
        return preparedSetters;
    }

    public enum Type {

        INTEGER,

        STRING

    }
}
