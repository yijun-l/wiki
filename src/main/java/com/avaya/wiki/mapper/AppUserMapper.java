package com.avaya.wiki.mapper;

import com.avaya.wiki.domain.AppUser;
import com.avaya.wiki.request.AppUserQueryRequest;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AppUserMapper {

    int create(AppUser appUser);

    AppUser getById(Long id);

    List<AppUser> list(AppUserQueryRequest appUserQueryRequest);

    int getTotal(AppUserQueryRequest appUserQueryRequest);

    int delete(Long id);
}
