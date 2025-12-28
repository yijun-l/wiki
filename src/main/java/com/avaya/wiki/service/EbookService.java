package com.avaya.wiki.service;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.exception.ResourceNotFoundException;
import com.avaya.wiki.mapper.EbookMapper;
import com.avaya.wiki.request.EbookQuery;
import com.avaya.wiki.request.EbookSaveRequest;
import com.avaya.wiki.request.EbookUpdateRequest;
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

    public PageResponse<EbookResponse> list(EbookQuery ebookQuery) {
        PageResponse<EbookResponse> pageResponse = new PageResponse<>();
        pageResponse.setTotal(ebookMapper.getTotal());
        List<Ebook> ebookList = ebookMapper.list(ebookQuery);
        ArrayList<EbookResponse> ebookResponseList = new ArrayList<>();
        for (Ebook ebook : ebookList) {
            EbookResponse ebookResponse = new EbookResponse();
            BeanUtils.copyProperties(ebook, ebookResponse);
            ebookResponseList.add(ebookResponse);
        }
        pageResponse.setRecords(ebookResponseList);

        return pageResponse;
    }

    public void save(EbookSaveRequest ebookSaveRequest) {
        Ebook ebook = new Ebook();
        BeanUtils.copyProperties(ebookSaveRequest, ebook);
        if (ebook.getId() == 0) {

        } else {
            ebookMapper.update(ebook);
        }
    }

    public EbookResponse getById(Long id) {
        if (!ebookMapper.existsById(id)) {
            throw new ResourceNotFoundException("Ebook not found" + id);
        }

        Ebook ebook = ebookMapper.getById(id);
        EbookResponse ebookResponse = new EbookResponse();
        BeanUtils.copyProperties(ebook, ebookResponse);

        return ebookResponse;
    }

    public void update(Long id, EbookUpdateRequest ebookUpdateRequest) {
        if (!ebookMapper.existsById(id)) {
            throw new ResourceNotFoundException("Ebook not found" + id);
        }

        Ebook ebook = new Ebook();
        ebook.setId(id);
        BeanUtils.copyProperties(ebookUpdateRequest, ebook);

        ebookMapper.update(ebook);
    }

}
