package com.avaya.wiki.request;

public class PageQuery {
    private int page = 1;
    private int size = 10;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    // MyBatis resolves parameter references by calling getter methods,
    // not by checking whether the underlying fields actually exist.
    public int getOffset() {
        return (page - 1) * size;
    }

    public int getLimit() {
        return size;
    }
}
