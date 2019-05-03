package ru.saidgadjiev.bibliographya.data.mapper;

import ru.saidgadjiev.bibliographya.data.ClientQueryVisitor;
import ru.saidgadjiev.bibliographya.domain.Biography;
import ru.saidgadjiev.bibliographya.model.BiographyModerationResponse;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by said on 02/05/2019.
 */
public class BiographyModerationFieldsMapper implements FieldsMapper {

    private final Collection<String> fields = Arrays.asList(Biography.MODERATOR_ID, Biography.MODERATION_STATUS);

    @Override
    public boolean has(String field) {
        return fields.contains(field);
    }

    @Override
    public ClientQueryVisitor.Type getType(String property) {
        switch (property) {
            case BiographyModerationResponse.MODERATOR_ID:
                return ClientQueryVisitor.Type.INTEGER;
            case BiographyModerationResponse.MODERATION_STATUS:
                return ClientQueryVisitor.Type.INTEGER;
        }

        return null;
    }

    @Override
    public String getField(String property) {
        switch (property) {
            case BiographyModerationResponse.MODERATOR_ID:
                return Biography.MODERATOR_ID;
            case BiographyModerationResponse.MODERATION_STATUS:
                return Biography.MODERATION_STATUS;
        }

        return null;
    }
}
