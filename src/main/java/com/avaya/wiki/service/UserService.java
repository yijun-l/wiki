package com.avaya.wiki.service;

import com.avaya.wiki.domain.User;
import com.avaya.wiki.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public List<User> getAllUsers() {
        return userMapper.findAll();
    }
}
