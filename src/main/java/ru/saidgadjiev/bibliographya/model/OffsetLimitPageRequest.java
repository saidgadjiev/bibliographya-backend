package ru.saidgadjiev.bibliographya.model;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Objects;

/**
 * Created by said on 20.11.2018.
 */
public class OffsetLimitPageRequest implements Pageable {

    private int limit;

    private long offset;

    private Sort sort;

    private Integer endAt;

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

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Integer getEndAt() {
        return endAt;
    }

    public void setEndAt(Integer endAt) {
        this.endAt = endAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OffsetLimitPageRequest that = (OffsetLimitPageRequest) o;
        return limit == that.limit &&
                offset == that.offset &&
                Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(limit, offset, sort);
    }

    public static class Builder {

        private int limit;

        private long offset;

        private Sort sort;

        private Integer endAt;

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

        public Builder setEndAt(Integer endAt) {
            this.endAt = endAt;

            return this;
        }

        public OffsetLimitPageRequest build() {
            OffsetLimitPageRequest offsetLimitPageRequest = new OffsetLimitPageRequest();

            offsetLimitPageRequest.limit = limit;
            offsetLimitPageRequest.offset = offset;
            offsetLimitPageRequest.sort = sort;
            offsetLimitPageRequest.endAt = endAt;

            return offsetLimitPageRequest;
        }
    }
}
