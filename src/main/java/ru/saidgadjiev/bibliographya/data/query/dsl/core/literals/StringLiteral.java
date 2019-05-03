package ru.saidgadjiev.bibliographya.data.query.dsl.core.literals;

import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;

/**
 * String literal.
 *
 * @author Said Gadjiev
 */
public class StringLiteral implements Literal<String> {

    /**
     * Current value.
     */
    private final String value;

    /**
     * Create a new instance.
     * @param value target value
     */
    public StringLiteral(String value) {
        this.value = value;
    }

    @Override
    public String getOriginal() {
        return value;
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }
}
