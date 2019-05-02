package ru.saidgadjiev.bibliographya.data.query.dsl.core.condition;

import ru.saidgadjiev.ormnext.core.query.visitor.QueryVisitor;
import ru.saidgadjiev.ormnext.core.query.visitor.element.Operand;

/**
 * Operand condition. It may be used for add aggregate function restriction eg. count(column_name) greater than 5.
 *
 * @author Said Gadjiev
 */
public class OperandCondition implements Condition {

    /**
     * Condition operand.
     * @see Operand
     */
    private Operand operand;

    /**
     * Create a new instance.
     * @param operand target operand
     */
    public OperandCondition(Operand operand) {
        this.operand = operand;
    }

    /**
     * Return current operand.
     * @return current operand
     */
    public Operand getOperand() {
        return operand;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        if (visitor.visit(this)) {
            operand.accept(visitor);
        }
    }
}
