package ru.saidgadjiev.bibliographya.dao.impl.dsl;

import org.apache.commons.lang.StringUtils;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.Alias;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.*;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.function.Lower;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.BooleanLiteral;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.IntLiteral;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.StringLiteral;

import java.util.Iterator;

/**
 * Created by said on 02/05/2019.
 */
public class DslVisitor implements QueryVisitor {

    /**
     * Use for build clause.
     */
    private StringBuilder clause = new StringBuilder();

    private String alias;

    private String escapeLiteral = "\"";

    public DslVisitor(String alias) {
        this.alias = alias;
    }

    @Override
    public void visit(StringLiteral stringLiteral) {
        if (stringLiteral.getOriginal() == null) {
            clause.append("null");
        } else {
            clause.append(escapeLiteral).append(stringLiteral.getOriginal()).append(escapeLiteral);
        }
    }

    @Override
    public boolean visit(Expression expression) {
        for (Iterator<AndCondition> iterator = expression.getConditions().iterator(); iterator.hasNext();) {
            AndCondition andCondition = iterator.next();

            if (andCondition.getConditions().isEmpty()) {
                continue;
            }

            clause.append("(");

            for (Iterator<Condition> conditionIterator = andCondition.getConditions().iterator();
                 conditionIterator.hasNext();) {
                Condition condition = conditionIterator.next();

                clause.append("(");

                condition.accept(this);

                clause.append(")");

                if (conditionIterator.hasNext()) {
                    clause.append(" AND ");
                }
            }

            clause.append(")");

            if (iterator.hasNext()) {
                clause.append(" OR ");
            }
        }

        return false;
    }

    @Override
    public boolean visit(AndCondition andCondition) {
        return true;
    }

    @Override
    public boolean visit(Equals equals) {
        equals.getFirst().accept(this);
        clause.append(" = ");
        equals.getSecond().accept(this);

        return false;
    }

    @Override
    public boolean visit(ColumnSpec columnSpec) {
        if (StringUtils.isNotBlank(alias)) {
            clause.append(alias).append(".");
        }
        clause.append(escapeLiteral).append(columnSpec.getName()).append(escapeLiteral);

        return false;
    }

    @Override
    public void visit(IntLiteral intLiteral) {
        clause.append(intLiteral.getOriginal());
    }

    @Override
    public void visit(Param param) {
        clause.append("?");
    }

    @Override
    public boolean visit(GreaterThan greaterThan) {
        return false;
    }

    @Override
    public boolean visit(GreaterThanOrEquals greaterThanOrEquals) {
        return false;
    }

    @Override
    public boolean visit(LessThan lessThan) {
        return false;
    }

    @Override
    public boolean visit(LessThanOrEquals lessThanOrEquals) {
        return false;
    }

    @Override
    public boolean visit(NotNull notNull) {
        notNull.getOperand().accept(this);
        clause.append(" IS NOT NULL");

        return false;
    }

    @Override
    public boolean visit(IsNull isNull) {
        isNull.getOperand().accept(this);
        clause.append(" IS NULL");

        return false;
    }

    @Override
    public boolean visit(NotEquals notEquals) {
        notEquals.getFirst().accept(this);
        clause.append(" != ");
        notEquals.getSecond().accept(this);

        return false;
    }

    @Override
    public boolean visit(Like like) {
        like.getOperand().accept(this);
        clause.append(" LIKE '").append(like.getPattern()).append("'");

        return false;
    }

    @Override
    public void visit(BooleanLiteral booleanLiteral) {
        clause.append(booleanLiteral);
    }

    @Override
    public void visit(Similar similar) {
        similar.getOperand().accept(this);
        clause.append(" SIMILAR TO '").append(similar.getPattern()).append("'");
    }

    @Override
    public void visit(Alias alias) {
        clause.append("'").append(alias.getAlias()).append("'");
    }

    @Override
    public void visit(Lower lower) {
        clause.append("lower(");
        lower.getOperand().accept(this);
        clause.append(")");
    }

    public String getClause() {
        return clause.toString();
    }
}
