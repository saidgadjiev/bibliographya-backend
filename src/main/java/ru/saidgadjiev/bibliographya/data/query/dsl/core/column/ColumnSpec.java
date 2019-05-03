package ru.saidgadjiev.bibliographya.data.query.dsl.core.column;


import ru.saidgadjiev.bibliographya.data.query.dsl.core.Alias;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.Operand;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.QueryVisitor;

/**
 * This class represent column in select except in select column list.
 *
 * @author Said Gadjiev
 */
public class ColumnSpec implements Operand {

    /**
     * Column name.
     */
    private String name;

    /**
     * Column alias.
     * @see Alias
     */
    private Alias alias;

    /**
     * Create new instance with provided column name.
     * @param name target column name
     */
    public ColumnSpec(String name) {
        this.name = name;
    }

    /**
     * Create new instance with provided column name and alias.
     * @param name target column name
     * @param alias target column alias
     */
    public ColumnSpec(String name, String alias) {
        this.name = name;
        this.alias = new Alias(alias);
    }

    /**
     * Provide column alias.
     * @param alias target column alias
     * @return this instance for chain
     */
    public ColumnSpec alias(String alias) {
        this.alias = new Alias(alias);

        return this;
    }

    /**
     * Return current column alias.
     * @return alias
     */
    public Alias getAlias() {
        return alias;
    }

    /**
     * Return current column name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Provide column name.
     * @param name target column name
     * @return this instance for chain
     */
    public ColumnSpec name(String name) {
        this.name = name;

        return this;
    }

    @Override
    public void accept(QueryVisitor visitor) {
        if (visitor.visit(this)) {
            alias.accept(visitor);
        }
    }

}
