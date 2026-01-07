package com.avaya.wiki.service;

import com.avaya.wiki.domain.AppUser;
import com.avaya.wiki.mapper.AppUserMapper;
import com.avaya.wiki.response.AppUserResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {
    private final AppUserMapper appUserMapper;

    public AppUserService(AppUserMapper appUserMapper) {
        this.appUserMapper = appUserMapper;
    }

    public AppUserResponse getById(Long id) {
        AppUser appUser = appUserMapper.getById(id);
        AppUserResponse appUserResponse = new AppUserResponse();
        BeanUtils.copyProperties(appUser, appUserResponse);
        appUserResponse.setStatus(appUser.getStatus().getDbValue());
        return appUserResponse;
    }

}
