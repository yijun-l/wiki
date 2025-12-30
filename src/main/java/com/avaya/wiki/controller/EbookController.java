package com.avaya.wiki.controller;

import com.avaya.wiki.request.EbookQueryRequest;
import com.avaya.wiki.request.EbookUpdateRequest;
import com.avaya.wiki.response.CommonResponse;
import com.avaya.wiki.response.EbookResponse;
import com.avaya.wiki.response.PageResponse;
import com.avaya.wiki.service.EbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ebooks")
public class EbookController {
    private final EbookService ebookService;

    @GetMapping
    public CommonResponse<PageResponse<EbookResponse>> list(EbookQueryRequest ebookQueryRequest) {
        return CommonResponse.success(ebookService.list(ebookQueryRequest));
    }

    @GetMapping("/{id}")
    public CommonResponse<EbookResponse> getById(@PathVariable Long id) {
        return CommonResponse.success(ebookService.getById(id));
    }

    @PatchMapping("/{id}")
    public CommonResponse<Void> updatePartial(
            @PathVariable Long id,
            @RequestBody EbookUpdateRequest ebookUpdateRequest) {
        ebookService.update(id, ebookUpdateRequest);
        return CommonResponse.success(null);
    }

    @PutMapping("/{id}")
    public CommonResponse<Void> update(
            @PathVariable Long id,
            @RequestBody EbookUpdateRequest ebookUpdateRequest) {
        ebookService.update(id, ebookUpdateRequest);
        return CommonResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public CommonResponse<Void> delete(@PathVariable Long id) {
        ebookService.delete(id);
        return CommonResponse.success(null);
    }
}
