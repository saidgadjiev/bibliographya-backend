package ru.saidgadjiev.bibliographya.data.mapper;

import ru.saidgadjiev.bibliographya.data.ClientQueryVisitor;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by said on 03/05/2019.
 */
public class GetUsersFieldsMapper implements FieldsMapper {

    private final Collection<String> fields = Collections.singletonList("role_name");

    @Override
    public boolean has(String field) {
        return fields.contains(field);
    }

    @Override
    public ClientQueryVisitor.Type getType(String property) {
        switch (property) {
            case "role_name":
                return ClientQueryVisitor.Type.STRING;
        }

        return null;
    }

    @Override
    public String getField(String property) {
        switch (property) {
            case "role_name":
                return "role_name";
        }

        return null;
    }
}
