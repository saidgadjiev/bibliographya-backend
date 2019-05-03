package ru.saidgadjiev.bibliographya.data.query.dsl.core.literals;

import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;

/**
 * Boolean literal.
 *
 * @author Said Gadjiev
 */
public class BooleanLiteral implements Literal<Boolean> {

    /**
     * Current value.
     */
    private final boolean value;

    /**
     * Create a new instance.
     * @param value target value
     */
    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    @Override
    public String getOriginal() {
        return String.valueOf(value);
    }

    @Override
    public Boolean get() {
        return value;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);

    }
}
