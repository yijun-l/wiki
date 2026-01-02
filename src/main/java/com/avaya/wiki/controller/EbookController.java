package com.avaya.wiki.controller;

import com.avaya.wiki.request.EbookCreateRequest;
import com.avaya.wiki.request.EbookQueryRequest;
import com.avaya.wiki.request.EbookUpdateRequest;
import com.avaya.wiki.response.CommonResponse;
import com.avaya.wiki.response.EbookResponse;
import com.avaya.wiki.response.PageResponse;
import com.avaya.wiki.service.EbookService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/ebooks")
public class EbookController {
    private final EbookService ebookService;

    public EbookController(EbookService ebookService) {
        this.ebookService = ebookService;
    }

    @PostMapping
    public CommonResponse<Long> create(@Valid @RequestBody EbookCreateRequest ebookCreateRequest) {
        Long id = ebookService.create(ebookCreateRequest);
        return CommonResponse.success(id);
    }

    @GetMapping
    public CommonResponse<PageResponse<EbookResponse>> list(@Valid @ModelAttribute EbookQueryRequest ebookQueryRequest) {
        return CommonResponse.success(ebookService.list(ebookQueryRequest));
    }

    @GetMapping("/{id}")
    public CommonResponse<EbookResponse> getById(@PathVariable @Min(1) Long id) {
        return CommonResponse.success(ebookService.getById(id));
    }

    @PatchMapping("/{id}")
    public CommonResponse<Void> updatePartial(
            @PathVariable Long id,
            @Valid @RequestBody EbookUpdateRequest ebookUpdateRequest) {
        ebookService.update(id, ebookUpdateRequest);
        return CommonResponse.success(null);
    }

    @PutMapping("/{id}")
    public CommonResponse<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody EbookUpdateRequest ebookUpdateRequest) {
        ebookService.update(id, ebookUpdateRequest);
        return CommonResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> delete(@PathVariable Long id) {
        ebookService.delete(id);
        return CommonResponse.success(null);
    }
}
