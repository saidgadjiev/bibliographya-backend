package ru.saidgadjiev.bibliographya.data.mapper;

import ru.saidgadjiev.bibliographya.data.ClientQueryVisitor;
import ru.saidgadjiev.bibliographya.domain.BiographyFix;
import ru.saidgadjiev.bibliographya.model.BiographyFixResponse;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by said on 03/05/2019.
 */
public class BiographyFixFieldsMapper implements FieldsMapper {

    private final Collection<String> fields = Arrays.asList(BiographyFix.FIXER_ID, BiographyFix.STATUS);

    @Override
    public boolean has(String field) {
        return fields.contains(field);
    }

    @Override
    public ClientQueryVisitor.Type getType(String property) {
        switch (property) {
            case BiographyFixResponse.FIXER_ID:
            case BiographyFixResponse.STATUS:
                return ClientQueryVisitor.Type.INTEGER;
        }

        return null;
    }

    @Override
    public String getField(String property) {
        switch (property) {
            case BiographyFixResponse.FIXER_ID:
                return BiographyFix.FIXER_ID;
            case BiographyFixResponse.STATUS:
                return BiographyFix.STATUS;
        }

        return null;
    }
}
