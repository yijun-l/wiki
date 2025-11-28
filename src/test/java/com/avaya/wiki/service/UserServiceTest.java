package com.avaya.wiki.service;

import com.avaya.wiki.domain.User;
import com.avaya.wiki.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getAllUsers_ShouldReturnUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("Alice");
        user1.setEmail("alice@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("Bob");
        user2.setEmail("bob@example.com");

        when(userMapper.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> result = userService.getAllUsers();
        assertEquals(2, result.size());
        assertEquals("Alice", result.get(0).getUsername());
        assertEquals("Bob", result.get(1).getUsername());
    }
}
