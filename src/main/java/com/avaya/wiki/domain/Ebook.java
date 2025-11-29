package com.avaya.wiki.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Ebook {
    private Long id;
    private String name;
    private Long cat1Id;
    private Long cat2Id;
    private String descText;
    private String coverUrl;
    private String docUrl;
    private String docType;
    private String version;
    private Integer views;
    private Integer likes;
}
