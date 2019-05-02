package ru.saidgadjiev.bibliographya.data.query.dsl.core.condition;

import ru.saidgadjiev.ormnext.core.query.visitor.QueryVisitor;

/**
 * This class represent not restriction.
 *
 * @author Said Gadjiev
 */
public class Not implements Condition {

    /**
     * Not condition.
     * @see Condition
     */
    private final Condition condition;

    /**
     * Create a new instance.
     * @param condition target condition
     */
    public Not(Condition condition) {
        this.condition = condition;
    }

    /**
     * Get current condition.
     * @return condition
     */
    public Condition getCondition() {
        return condition;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        if (visitor.visit(this)) {
            condition.accept(visitor);
        }
    }
}
