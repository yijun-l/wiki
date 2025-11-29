package com.avaya.wiki.service;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.mapper.EbookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EbookService {
    private final EbookMapper ebookMapper;

    public List<Ebook> list(){
        return ebookMapper.list();
    }

}
