package com.avaya.wiki.controller;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.request.EbookQuery;
import com.avaya.wiki.service.EbookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EbookController.class)
public class EbookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EbookService ebookService;

    @Test
    void list_ShouldReturnSuccessResponseWithEbooks() throws Exception {
        Ebook ebook1 = new Ebook();
        ebook1.setId(1L);
        ebook1.setName("ebook1");
        Ebook ebook2 = new Ebook();
        ebook2.setId(2L);
        ebook2.setName("ebook2");

        when(ebookService.list(any(EbookQuery.class))).thenReturn(Arrays.asList(ebook1, ebook2));

        mockMvc.perform(get("/ebook/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("200 OK"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("ebook1"))
                .andExpect(jsonPath("$.data[1].id").value(2))
                .andExpect(jsonPath("$.data[1].name").value("ebook2"));
    }
}
