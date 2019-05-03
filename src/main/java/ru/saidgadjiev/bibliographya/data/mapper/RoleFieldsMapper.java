package ru.saidgadjiev.bibliographya.data.mapper;

import ru.saidgadjiev.bibliographya.data.ClientQueryVisitor;
import ru.saidgadjiev.bibliographya.domain.Role;

import java.util.Collection;
import java.util.Collections;

/**
 * Created by said on 03/05/2019.
 */
public class RoleFieldsMapper implements FieldsMapper {

    private final Collection<String> fields = Collections.singletonList(Role.NAME);

    @Override
    public boolean has(String field) {
        return fields.contains(field);
    }

    @Override
    public ClientQueryVisitor.Type getType(String property) {
        switch (property) {
            case Role.NAME:
                return ClientQueryVisitor.Type.STRING;
        }

        return null;
    }

    @Override
    public String getField(String property) {
        switch (property) {
            case Role.NAME:
                return Role.NAME;
        }

        return null;
    }
}
