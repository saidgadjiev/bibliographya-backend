package ru.saidgadjiev.bibliographya.data.mapper;

import ru.saidgadjiev.bibliographya.data.ClientQueryVisitor;
import ru.saidgadjiev.bibliographya.domain.Bug;
import ru.saidgadjiev.bibliographya.model.BugResponse;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by said on 03/05/2019.
 */
public class BugFieldsMapper implements FieldsMapper {

    private final Collection<String> fields = Arrays.asList(Bug.FIXER_ID, Bug.STATUS);

    @Override
    public boolean has(String field) {
        return fields.contains(field);
    }

    @Override
    public ClientQueryVisitor.Type getType(String property) {
        switch (property) {
            case BugResponse.FIXER_ID:
                return ClientQueryVisitor.Type.INTEGER;
            case BugResponse.STATUS:
                return ClientQueryVisitor.Type.INTEGER;
        }

        return null;
    }

    @Override
    public String getField(String property) {
        switch (property) {
            case BugResponse.FIXER_ID:
                return Bug.FIXER_ID;
            case BugResponse.STATUS:
                return Bug.STATUS;
        }

        return null;
    }
}
