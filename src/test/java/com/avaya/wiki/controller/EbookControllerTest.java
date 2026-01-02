package com.avaya.wiki.controller;

import com.avaya.wiki.request.EbookQueryRequest;
import com.avaya.wiki.response.EbookResponse;
import com.avaya.wiki.response.PageResponse;
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
        EbookResponse ebookResponse1 = new EbookResponse();
        ebookResponse1.setId(1L);
        ebookResponse1.setName("ebook1");
        EbookResponse ebookResponse2 = new EbookResponse();
        ebookResponse2.setId(2L);
        ebookResponse2.setName("ebook2");
        PageResponse<EbookResponse> pageResponse = new PageResponse<>();
        pageResponse.setRecords(Arrays.asList(ebookResponse1, ebookResponse2));

        when(ebookService.list(any(EbookQueryRequest.class))).thenReturn(pageResponse);

        mockMvc.perform(get("/ebooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.records").isArray())
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].id").value(1))
                .andExpect(jsonPath("$.data.records[0].name").value("ebook1"))
                .andExpect(jsonPath("$.data.records[1].id").value(2))
                .andExpect(jsonPath("$.data.records[1].name").value("ebook2"));
    }
}
