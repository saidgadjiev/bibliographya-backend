package ru.saidgadjiev.bibliographya.data.query.dsl.core.condition;

import ru.saidgadjiev.bibliographya.data.query.dsl.core.Operand;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;

/**
 * Created by said on 02/05/2019.
 */
public class Similar implements Condition {

    private Operand operand;

    private String pattern;

    public Similar(Operand operand, String pattern) {
        this.operand = operand;
        this.pattern = pattern;
    }

    public Operand getOperand() {
        return operand;
    }

    public String getPattern() {
        return pattern;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }
}
