package com.avaya.wiki.mapper;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.request.EbookQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EbookMapper {

    int create(Ebook ebook);

    List<Ebook> list(EbookQueryRequest ebookQueryRequest);

    int getTotal();

    Ebook getById(Long id);

    int update(Ebook ebook);

    int delete(Long id);

    int existsByIdRaw(@Param("id") Long id);

    default boolean existsById(Long id) {
        return existsByIdRaw(id) == 1;
    }
}
