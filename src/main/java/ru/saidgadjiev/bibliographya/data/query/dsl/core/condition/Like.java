package ru.saidgadjiev.bibliographya.data.query.dsl.core.condition;

import ru.saidgadjiev.bibliographya.data.query.dsl.core.Operand;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;

/**
 * This class represent like restriction.
 *
 * @author Said Gadjiev
 */
public class Like implements Condition {

    /**
     * Checked value.
     * @see Operand
     */
    private final Operand operand;

    /**
     * Like pattern.
     */
    private final String pattern;

    /**
     * Create a new instance.
     * @param operand target checked value
     * @param pattern target like pattern
     */
    public Like(Operand operand, String pattern) {
        this.operand = operand;
        this.pattern = pattern;
    }

    /**
     * Return current operand checked value.
     * @return operand
     */
    public Operand getOperand() {
        return operand;
    }

    /**
     * Return current like pattern.
     * @return pattern
     */
    public String getPattern() {
        return pattern;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        if (visitor.visit(this)) {
            operand.accept(visitor);
        }
    }
}
