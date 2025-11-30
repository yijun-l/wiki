package com.avaya.wiki.controller;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.response.CommonResponse;
import com.avaya.wiki.service.EbookService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/ebook")
public class EbookController {
    private final EbookService ebookService;

    @GetMapping("/list")
    public CommonResponse<List<Ebook>> list() {
        CommonResponse<List<Ebook>> commonResponse = new CommonResponse<List<Ebook>>();
        commonResponse.setSuccess(true);
        commonResponse.setMessage("200 OK");
        commonResponse.setData(ebookService.list());
        return commonResponse;
    }
}
