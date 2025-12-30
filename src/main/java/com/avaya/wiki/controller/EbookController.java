package com.avaya.wiki.controller;

import com.avaya.wiki.request.EbookQuery;
import com.avaya.wiki.request.EbookQueryRequest;
import com.avaya.wiki.request.EbookSaveRequest;
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

    @GetMapping("/list")
    public CommonResponse list(EbookQuery ebookQuery) {
        CommonResponse<PageResponse<EbookResponse>> commonResponse = new CommonResponse<>();
        commonResponse.setSuccess(true);
        commonResponse.setMessage("200 OK");
        commonResponse.setData(ebookService.list(ebookQuery));
        return commonResponse;
    }

    @PostMapping("/save")
    public CommonResponse save(@RequestBody EbookSaveRequest ebookSaveRequest) {
        CommonResponse commonResponse = new CommonResponse();
        ebookService.save(ebookSaveRequest);
        return commonResponse;
    }

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

    @DeleteMapping("/{id}")
    public CommonResponse<Void> delete(@PathVariable Long id) {
        ebookService.delete(id);
        return CommonResponse.success(null);
    }
}
