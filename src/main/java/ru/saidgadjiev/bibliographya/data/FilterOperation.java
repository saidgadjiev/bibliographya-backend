package ru.saidgadjiev.bibliographya.data;

import ru.saidgadjiev.bibliographya.data.operator.Operator;

/**
 * Created by said on 24.11.2018.
 */
public enum FilterOperation implements Operator {

    EQ {
        @Override
        public FilterOperation getType() {
            return EQ;
        }
    },
    FIELDS_EQ {
        @Override
        public FilterOperation getType() {
            return FIELDS_EQ;
        }
    },
    FIELDS_NOT_EQ {
        @Override
        public FilterOperation getType() {
            return FIELDS_NOT_EQ;
        }
    },
    NOT_EQ {
        @Override
        public FilterOperation getType() {
            return NOT_EQ;
        }
    },
    LIKE {
        @Override
        public FilterOperation getType() {
            return LIKE;
        }
    },
    IS_NOT_NULL {
        @Override
        public FilterOperation getType() {
            return IS_NOT_NULL;
        }
    },
    IS_NULL {
        @Override
        public FilterOperation getType() {
            return IS_NULL;
        }
    },
    SIMILAR {
        @Override
        public FilterOperation getType() {
            return SIMILAR;
        }
    };

    @Override
    public String getClause(FilterCriteria criteria) {
        return null;
    }
}
