package com.avaya.wiki.mapper;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.request.EbookQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EbookMapper {
    List<Ebook> list(EbookQuery query);

    int getTotal();

    int update(Ebook ebook);

    int existsByIdRaw(@Param("id") Long id);

    default boolean existsById(Long id) {
        return existsByIdRaw(id) == 1;
    }
}
