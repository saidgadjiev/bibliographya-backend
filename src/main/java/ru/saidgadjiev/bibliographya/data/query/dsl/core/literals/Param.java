package ru.saidgadjiev.bibliographya.data.query.dsl.core.literals;

import ru.saidgadjiev.ormnext.core.query.visitor.QueryVisitor;
import ru.saidgadjiev.ormnext.core.query.visitor.element.Operand;

/**
 * This class use for prepared statement param '?' instead of r_value.
 *
 * @author Said Gadjiev
 */
public class Param implements RValue, Operand {

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }
}
