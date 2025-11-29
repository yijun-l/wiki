package com.avaya.wiki.mapper;

import com.avaya.wiki.domain.Ebook;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EbookMapper {
    @Select("SELECT * FROM public.ebook")
    List<Ebook> list();
}
