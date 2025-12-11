package com.avaya.wiki.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EbookQuery extends PageQuery {
    private Long id;
    private String name;
}
