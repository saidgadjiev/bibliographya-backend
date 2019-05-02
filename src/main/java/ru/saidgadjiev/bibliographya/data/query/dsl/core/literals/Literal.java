package ru.saidgadjiev.bibliographya.data.query.dsl.core.literals;

import ru.saidgadjiev.ormnext.core.query.visitor.element.Operand;

/**
 * This is the abstract class for implement literal.
 *
 * @param <T> literal type
 * @author Said Gadjiev
 */
public interface Literal<T> extends RValue, Operand {

    /**
     * Get sql literal present.
     *
     * @return sql present for current literal
     */
    String getOriginal();

    /**
     * Get current literal.
     *
     * @return current literal.
     */
    T get();
}
