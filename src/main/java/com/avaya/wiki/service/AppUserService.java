package com.avaya.wiki.service;

import com.avaya.wiki.domain.AppUser;
import com.avaya.wiki.exception.ResourceNotFoundException;
import com.avaya.wiki.mapper.AppUserMapper;
import com.avaya.wiki.request.AppUserQueryRequest;
import com.avaya.wiki.response.AppUserResponse;
import com.avaya.wiki.response.PageResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppUserService {
    private final AppUserMapper appUserMapper;

    public AppUserService(AppUserMapper appUserMapper) {
        this.appUserMapper = appUserMapper;
    }

    public AppUserResponse getById(Long id) {
        AppUser appUser = appUserMapper.getById(id);
        if (appUser == null) {
            throw new ResourceNotFoundException("User not found, id = " + id);
        }
        AppUserResponse appUserResponse = new AppUserResponse();
        BeanUtils.copyProperties(appUser, appUserResponse);
        appUserResponse.setStatus(appUser.getStatus().getDbValue());
        return appUserResponse;
    }

    public PageResponse<AppUserResponse> list(AppUserQueryRequest appUserQueryRequest) {
        PageResponse<AppUserResponse> pageResponse = new PageResponse<>();
        pageResponse.setTotal(appUserMapper.getTotal(appUserQueryRequest));
        List<AppUser> appUserList = appUserMapper.list(appUserQueryRequest);
        ArrayList<AppUserResponse> appUserResponses = new ArrayList<>(appUserList.size());
        for (AppUser appUser : appUserList) {
            appUserResponses.add(toResponse(appUser));
        }
        pageResponse.setRecords(appUserResponses);
        return pageResponse;
    }

    public void delete(Long id){
        if (appUserMapper.delete(id) == 0){
            // No such record in DB
            throw new ResourceNotFoundException("User not found, id = " + id);
        }
    }

    private AppUserResponse toResponse(AppUser appUser) {
        AppUserResponse response = new AppUserResponse();
        BeanUtils.copyProperties(appUser, response);
        return response;
    }

}
