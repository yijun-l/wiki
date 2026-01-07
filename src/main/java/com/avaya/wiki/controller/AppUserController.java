package com.avaya.wiki.controller;

import com.avaya.wiki.response.AppUserResponse;
import com.avaya.wiki.response.CommonResponse;
import com.avaya.wiki.service.AppUserService;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/{id}")
    public CommonResponse<AppUserResponse> getById(@PathVariable @Min(1) Long id) {
        return CommonResponse.success(appUserService.getById(id));
    }
}
