package ru.saidgadjiev.bibliographya.data.mapper;

import ru.saidgadjiev.bibliographya.data.ClientQueryVisitor;

/**
 * Created by said on 02/05/2019.
 */
public interface FieldsMapper {

    boolean has(String field);

    ClientQueryVisitor.Type getType(String property);

    String getField(String property);
}
