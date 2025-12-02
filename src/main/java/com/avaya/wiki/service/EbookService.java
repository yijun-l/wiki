package com.avaya.wiki.service;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.mapper.EbookMapper;
import com.avaya.wiki.request.EbookQuery;
import com.avaya.wiki.response.EbookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EbookService {
    private final EbookMapper ebookMapper;

    public List<EbookResponse> list(EbookQuery ebookQuery){
        List<Ebook> ebookList = ebookMapper.list(ebookQuery);
        ArrayList<EbookResponse> ebookResponseList = new ArrayList<>();
        for (Ebook ebook : ebookList){
            EbookResponse ebookResponse = new EbookResponse();
            BeanUtils.copyProperties(ebook, ebookResponse);
            ebookResponseList.add(ebookResponse);
        }
        return ebookResponseList;
    }

}
