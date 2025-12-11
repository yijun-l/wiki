package com.avaya.wiki.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PageQuery {
    private int page;
    private int size;

    // MyBatis resolves parameter references by calling getter methods,
    // not by checking whether the underlying fields actually exist.
    public int getOffset(){
        return (page - 1) * size;
    }

    public int getLimit(){
        return size;
    }
}
