package com.avaya.wiki.service;

import com.avaya.wiki.common.SnowflakeIdGenerator;
import com.avaya.wiki.domain.AppUser;
import com.avaya.wiki.domain.UserStatus;
import com.avaya.wiki.exception.ResourceNotFoundException;
import com.avaya.wiki.mapper.AppUserMapper;
import com.avaya.wiki.request.AppUserCreateRequest;
import com.avaya.wiki.request.AppUserQueryRequest;
import com.avaya.wiki.response.AppUserResponse;
import com.avaya.wiki.response.PageResponse;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AppUserService {
    private final AppUserMapper appUserMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(AppUserMapper appUserMapper, SnowflakeIdGenerator idGenerator, PasswordEncoder passwordEncoder) {

        this.appUserMapper = appUserMapper;
        this.idGenerator = idGenerator;
        this.passwordEncoder = passwordEncoder;
    }

    public Long create(AppUserCreateRequest appUserCreateRequest) {
        AppUser appUser = new AppUser();
        BeanUtils.copyProperties(appUserCreateRequest, appUser);
        Long id = idGenerator.getNextID();
        appUser.setId(id);
        appUser.setPasswordHash(passwordEncoder.encode(appUserCreateRequest.getPassword()));
        appUser.setStatus(UserStatus.ACTIVE);
        appUserMapper.create(appUser);
        return id;
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

    public void delete(Long id) {
        if (appUserMapper.delete(id) == 0) {
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
