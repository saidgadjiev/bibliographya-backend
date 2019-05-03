package ru.saidgadjiev.bibliographya.data.query.dsl.core;

/**
 * This class represent alias eg. 'test' as 'test1'.
 *
 * @author Said Gadjiev
 */
public class Alias implements Operand {

    /**
     * Alias.
     */
    private final String alias;

    /**
     * Create a new instance.
     * @param alias target alias
     */
    public Alias(String alias) {
        this.alias = alias;
    }

    /**
     * Return current alias.
     * @return current alias
     */
    public String getAlias() {
        return alias;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        visitor.visit(this);
    }
}
