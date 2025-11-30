package com.avaya.wiki.service;

import com.avaya.wiki.domain.Ebook;
import com.avaya.wiki.mapper.EbookMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

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
    void setUp(){
        ebook1 = new Ebook();
        ebook1.setId(1L);
        ebook1.setName("ebook1");
        ebook2 = new Ebook();
        ebook2.setId(2L);
        ebook2.setName("ebook2");
    }

    @Test
    void list_ShouldReturnAllEbooks() {
        when(ebookMapper.list()).thenReturn(Arrays.asList(ebook1, ebook2));

        List<Ebook> result = ebookService.list();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(ebook1));
        assertTrue(result.contains(ebook2));

        verify(ebookMapper).list();
        verifyNoMoreInteractions(ebookMapper);
    }


}
