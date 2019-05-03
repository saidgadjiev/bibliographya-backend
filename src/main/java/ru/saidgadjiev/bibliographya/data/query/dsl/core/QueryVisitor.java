package ru.saidgadjiev.bibliographya.data.query.dsl.core;


import ru.saidgadjiev.bibliographya.data.query.dsl.core.column.ColumnSpec;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.condition.*;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.function.Lower;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.BooleanLiteral;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.IntLiteral;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.Param;
import ru.saidgadjiev.bibliographya.data.query.dsl.core.literals.StringLiteral;

/**
 * Interface to make use of the Visitor pattern programming style.
 * I.e. a class that implements this interface can traverse the contents of
 * a Java class just by calling the `accept' method which all classes have.
 *
 * @author Said Gadjiev
 * @see QueryElement
 */
public interface QueryVisitor {

    void visit(StringLiteral stringLiteral);

    /**
     * Visit {@link Expression} element.
     *
     * @param expression target visitor element
     * @return true if need visit another visitor elements that contained in {@code expression} else false
     * @see Expression
     */
    boolean visit(Expression expression);

    /**
     * Visit {@link AndCondition} element.
     *
     * @param andCondition target visitor element
     * @return true if need visit another visitor elements that contained in {@code andCondition} else false
     * @see AndCondition
     */
    boolean visit(AndCondition andCondition);

    /**
     * Visit {@link Equals} element.
     *
     * @param equals target visitor element
     * @return true if need visit another visitor elements that contained in {@code equals} else false
     * @see Equals
     */
    boolean visit(Equals equals);

    /**
     * Visit {@link ColumnSpec} element.
     *
     * @param columnSpec target visitor element
     * @return true if need visit another visitor elements that contained in {@code columnSpec} else false
     * @see ColumnSpec
     */
    boolean visit(ColumnSpec columnSpec);

    /**
     * Visit {@link IntLiteral} element.
     *
     * @param intLiteral target visitor element
     * @see IntLiteral
     */
    void visit(IntLiteral intLiteral);

    /**
     * Visit {@link Param} element.
     *
     * @param param target visitor element
     * @see Param
     */
    void visit(Param param);

    /**
     * Visit {@link GreaterThan} element.
     *
     * @param greaterThan target visitor element
     * @return true if need visit another visitor elements that contained in {@code greaterThan} else false
     * @see GreaterThan
     */
    boolean visit(GreaterThan greaterThan);

    /**
     * Visit {@link GreaterThanOrEquals} element.
     *
     * @param greaterThanOrEquals target visitor element
     * @return true if need visit another visitor elements that contained in {@code greaterThanOrEquals} else false
     * @see GreaterThanOrEquals
     */
    boolean visit(GreaterThanOrEquals greaterThanOrEquals);

    /**
     * Visit {@link LessThan} element.
     *
     * @param lessThan target visitor element
     * @return true if need visit another visitor elements that contained in {@code lessThan} else false
     * @see LessThan
     */
    boolean visit(LessThan lessThan);

    /**
     * Visit {@link LessThanOrEquals} element.
     *
     * @param lessThanOrEquals target visitor element
     * @return true if need visit another visitor elements that contained in {@code lessThanOrEquals} else false
     * @see LessThanOrEquals
     */
    boolean visit(LessThanOrEquals lessThanOrEquals);

    /**
     * Visit {@link NotNull} element.
     *
     * @param notNull target visitor element
     * @return true if need visit another visitor elements that contained in {@code notNull} else false
     * @see NotNull
     */
    boolean visit(NotNull notNull);

    /**
     * Visit {@link IsNull} element.
     *
     * @param isNull target visitor element
     * @return true if need visit another visitor elements that contained in {@code isNull} else false
     * @see IsNull
     */
    boolean visit(IsNull isNull);

    /**
     * Visit {@link NotEquals} element.
     *
     * @param notEquals target visitor element
     * @return true if need visit another visitor elements that contained in {@code notEquals} else false
     * @see NotEquals
     */
    boolean visit(NotEquals notEquals);

    /**
     * Visit {@link Like} element.
     *
     * @param like target visitor element
     * @return true if need visit another visitor elements that contained in {@code like} else false
     * @see Like
     */
    boolean visit(Like like);

    /**
     * Visit {@link BooleanLiteral} element.
     *
     * @param booleanLiteral target visitor element
     * @see BooleanLiteral
     */
    void visit(BooleanLiteral booleanLiteral);

    void visit(Similar similar);

    void visit(Alias alias);

    void visit(Lower lower);
}
