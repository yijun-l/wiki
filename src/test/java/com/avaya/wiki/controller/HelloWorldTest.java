package com.avaya.wiki.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class HelloWorldTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void hello_ShouldReturnHelloWorld() throws Exception {
        mockMvc.perform(get("/hello")).
                andExpect(status().isOk()).
                andExpect(content().string("Hello World!"));
    }

    @Test
    void hello_WithDifferentHttpMethod_ShouldReturn405() throws Exception {
        mockMvc.perform(post("/hello")).
                andExpect(status().isMethodNotAllowed());
    }
}
