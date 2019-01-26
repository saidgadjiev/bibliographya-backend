package ru.saidgadjiev.bibliographya.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by said on 24.11.2018.
 */
public class Filter {

    private List<FilterCriteria> filterCriteria = new ArrayList<>();

    public void addCriteria(FilterCriteria criteria) {
        filterCriteria.add(criteria);
    }

    public List<FilterCriteria> getFilterCriteria() {
        return filterCriteria;
    }
}
