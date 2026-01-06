package com.avaya.wiki.mapper;

import com.avaya.wiki.domain.AppUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppUserMapper {
    AppUser getById(Long id);
}
