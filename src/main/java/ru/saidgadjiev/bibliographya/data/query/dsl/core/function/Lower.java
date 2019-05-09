package ru.saidgadjiev.bibliographya.data.query.dsl.core.function;

import ru.saidgadjiev.bibliographya.data.query.dsl.core.Operand;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;

public class Lower implements Operand {

    private Operand operand;

    public Lower(Operand operand) {
        this.operand = operand;
    }

    public Operand getOperand() {
        return operand;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }
}
