package com.avaya.wiki.request;

public class EbookQueryRequest extends PageQuery {
    private String name;
    private Long cat1Id;
    private Long cat2Id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCat1Id() {
        return cat1Id;
    }

    public void setCat1Id(Long cat1Id) {
        this.cat1Id = cat1Id;
    }

    public Long getCat2Id() {
        return cat2Id;
    }

    public void setCat2Id(Long cat2Id) {
        this.cat2Id = cat2Id;
    }


}
