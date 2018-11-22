package ru.saidgadjiev.bibliography.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Created by said on 20.11.2018.
 */
public class OffsetLimitPageRequest implements Pageable {

    private int limit;

    private long offset;

    private Sort sort;

    @Override
    public int getPageNumber() {
        return 0;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable next() {
        return null;
    }

    @Override
    public Pageable previousOrFirst() {
        return null;
    }

    @Override
    public Pageable first() {
        return null;
    }

    @Override
    public boolean hasPrevious() {
        return false;
    }

    public static class Builder {

        private int limit;

        private long offset;

        private Sort sort;

        public Builder setLimit(int limit) {
            this.limit = limit;

            return this;
        }

        public Builder setOffset(long offset) {
            this.offset = offset;

            return this;
        }

        public Builder setSort(Sort sort) {
            this.sort = sort;

            return this;
        }

        public OffsetLimitPageRequest build() {
            OffsetLimitPageRequest offsetLimitPageRequest = new OffsetLimitPageRequest();

            offsetLimitPageRequest.limit = limit;
            offsetLimitPageRequest.offset = offset;
            offsetLimitPageRequest.sort = sort;

            return offsetLimitPageRequest;
        }
    }
}
