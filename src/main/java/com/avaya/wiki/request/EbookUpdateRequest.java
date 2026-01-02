package com.avaya.wiki.request;

import jakarta.validation.constraints.*;

public class EbookUpdateRequest {
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Category 1 ID cannot be null")
    @Min(value = 1, message = "Category 1 ID must be greater than 0")
    private Long cat1Id;

    @NotNull(message = "Category 2 ID cannot be null")
    @Min(value = 1, message = "Category 2 ID must be greater than 0")
    private Long cat2Id;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String descText;

    @Size(max = 200, message = "Cover URL cannot exceed 200 characters")
    private String coverUrl;

    @NotBlank(message = "Document URL cannot be blank")
    @Size(max = 200, message = "Cover URL cannot exceed 200 characters")
    private String docUrl;

    @NotBlank(message = "Document type cannot be blank")
    @Size(max = 20, message = "Version cannot exceed 20 characters")
    private String docType;

    @NotBlank(message = "Version cannot be blank")
    @Size(max = 20, message = "Version cannot exceed 20 characters")
    private String version;

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

    public String getDescText() {
        return descText;
    }

    public void setDescText(String descText) {
        this.descText = descText;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getDocUrl() {
        return docUrl;
    }

    public void setDocUrl(String docUrl) {
        this.docUrl = docUrl;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
