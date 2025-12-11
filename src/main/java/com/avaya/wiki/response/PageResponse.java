package com.avaya.wiki.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class PageResponse<T> {
    private int total;
    private List<T> records;
}
