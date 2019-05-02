package ru.saidgadjiev.bibliographya.data.query.dsl.core.condition;

import ru.saidgadjiev.ormnext.core.query.visitor.QueryVisitor;
import ru.saidgadjiev.ormnext.core.query.visitor.element.Operand;
import ru.saidgadjiev.ormnext.core.query.visitor.element.SelectQuery;

/**
 * This class represent not in restriction.
 *
 * @author Said Gadjiev
 */
public class NotInSelect implements Condition {

    /**
     * Checked operand.
     * @see Operand
     */
    private Operand operand;

    /**
     * Not in sub executeQuery.
     * @see SelectQuery
     */
    private SelectQuery selectQuery;

    /**
     * Create new not in.
     * @param selectQuery target sub executeQuery
     * @param operand target checked operand
     */
    public NotInSelect(SelectQuery selectQuery, Operand operand) {
        this.selectQuery = selectQuery;
        this.operand = operand;
    }

    /**
     * Return current not in sub executeQuery.
     * @return selectQuery
     */
    public SelectQuery getSelectQuery() {
        return selectQuery;
    }

    /**
     * Return current checked operand.
     * @return operand
     */
    public Operand getOperand() {
        return operand;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        if (visitor.visit(this)) {
            operand.accept(visitor);
            selectQuery.accept(visitor);
        }
    }
}
