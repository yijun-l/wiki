package com.avaya.wiki.service;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.mapper.EbookMapper;
import com.avaya.wiki.request.EbookQuery;
import com.avaya.wiki.request.EbookSaveRequest;
import com.avaya.wiki.response.EbookResponse;
import com.avaya.wiki.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EbookService {
    private final EbookMapper ebookMapper;

    public PageResponse<EbookResponse> list(EbookQuery ebookQuery){
        PageResponse<EbookResponse> pageResponse = new PageResponse<>();
        pageResponse.setTotal(ebookMapper.getTotal());
        List<Ebook> ebookList = ebookMapper.list(ebookQuery);
        ArrayList<EbookResponse> ebookResponseList = new ArrayList<>();
        for (Ebook ebook : ebookList){
            EbookResponse ebookResponse = new EbookResponse();
            BeanUtils.copyProperties(ebook, ebookResponse);
            ebookResponseList.add(ebookResponse);
        }
        pageResponse.setRecords(ebookResponseList);

        return pageResponse;
    }

    public void save(EbookSaveRequest ebookSaveRequest){

    }

}
