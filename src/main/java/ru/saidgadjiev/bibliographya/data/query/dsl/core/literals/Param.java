package ru.saidgadjiev.bibliographya.data.query.dsl.core.literals;


import ru.saidgadjiev.bibliographya.data.query.dsl.core.Operand;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;

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
