package ru.saidgadjiev.bibliographya.data.query.dsl.core.literals;

import ru.saidgadjiev.ormnext.core.query.visitor.QueryVisitor;

/**
 * Float literal.
 *
 * @author Said Gadjiev
 */
public class FloatLiteral implements Literal<Float> {

    /**
     * Current value.
     */
    private final float value;

    /**
     * Create a new instance.
     * @param value target value
     */
    public FloatLiteral(float value) {
        this.value = value;
    }

    @Override
    public String getOriginal() {
        return String.valueOf(value);
    }

    @Override
    public Float get() {
        return value;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);

    }
}
