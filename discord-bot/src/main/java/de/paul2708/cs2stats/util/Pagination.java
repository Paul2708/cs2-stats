package de.paul2708.cs2stats.util;

import java.util.ArrayList;
import java.util.List;

public abstract class Pagination<T, R> {

    public static final int FIRST_PAGE = 1;

    private final List<T> items;
    private final int pageSize;

    public Pagination(List<T> items, int pageSize) {
        this.items = items;
        this.pageSize = pageSize;
    }

    public abstract R renderPage(int page);

    public boolean hasNextPage(int currentPage) {
        return (currentPage * pageSize) < items.size();
    }

    public boolean hasPreviousPage(int currentPage) {
        return currentPage > 1;
    }

    public List<T> sliceItems(int page) {
        List<T> slice = new ArrayList<>();
        for (int i = (page - 1) * pageSize; i < Math.min(page * pageSize, items.size()); i++) {
            slice.add(items.get(i));
        }

        return slice;
    }
}
