package ru.saidgadjiev.bibliographya.data.query.dsl.core.condition;

import ru.saidgadjiev.bibliographya.data.query.dsl.core.Operand;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;

/**
 * This class represent less than restriction.
 *
 * @author Said Gadjiev
 */
public class LessThan implements Condition {

    /**
     * Left checked value.
     * @see Operand
     */
    private final Operand first;

    /**
     * Left checked value.
     * @see Operand
     */
    private final Operand second;

    /**
     * Create a new instance.
     * @param first target left checked value.
     * @param second taget right checked value.
     */
    public LessThan(Operand first, Operand second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Return current left checked value.
     * @return first
     */
    public Operand getFirst() {
        return first;
    }

    /**
     * Return current right checked value.
     * @return second
     */
    public Operand getSecond() {
        return second;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        if (visitor.visit(this)) {
            first.accept(visitor);
            second.accept(visitor);
        }
    }
}
