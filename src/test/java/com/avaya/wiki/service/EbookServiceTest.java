package com.avaya.wiki.service;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.mapper.EbookMapper;
import com.avaya.wiki.request.EbookQueryRequest;
import com.avaya.wiki.response.EbookResponse;
import com.avaya.wiki.response.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EbookServiceTest {
    @Mock
    private EbookMapper ebookMapper;

    @InjectMocks
    private EbookService ebookService;

    private Ebook ebook1, ebook2;

    @BeforeEach
    void setUp() {
        ebook1 = new Ebook();
        ebook1.setId(1L);
        ebook1.setName("ebook1");
        ebook2 = new Ebook();
        ebook2.setId(2L);
        ebook2.setName("ebook2");
    }

    @Test
    void list_ShouldReturnAllEbooks() {
        EbookQueryRequest ebookQueryRequest = new EbookQueryRequest();
        when(ebookMapper.list(any(EbookQueryRequest.class))).thenReturn(Arrays.asList(ebook1, ebook2));

        PageResponse<EbookResponse> result = ebookService.list(ebookQueryRequest);
        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        assertEquals("ebook1", result.getRecords().get(0).getName());
        assertEquals("ebook2", result.getRecords().get(1).getName());

        verify(ebookMapper).list(ebookQueryRequest);
    }


}
