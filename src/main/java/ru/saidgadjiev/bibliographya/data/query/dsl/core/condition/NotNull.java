package ru.saidgadjiev.bibliographya.data.query.dsl.core.condition;

import ru.saidgadjiev.bibliographya.data.query.dsl.core.Operand;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;

/**
 * This class represent is not null restriction.
 *
 * @author Said Gadjiev
 */
public class NotNull implements Condition {

    /**
     * Checked operand.
     * @see Operand
     */
    private final Operand operand;

    /**
     * Create a new instance.
     * @param operand target checked operand
     */
    public NotNull(Operand operand) {
        this.operand = operand;
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
        }
    }
}
