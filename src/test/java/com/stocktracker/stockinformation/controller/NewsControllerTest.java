package com.stocktracker.stockinformation.controller;

import com.stocktracker.stockinformation.model.Article;
import com.stocktracker.stockinformation.model.NewsApiResponse;
import com.stocktracker.stockinformation.model.Source;
import com.stocktracker.stockinformation.service.NewsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsController.class)
public class NewsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    NewsServiceImpl newsServiceImpl;

    private final String SYMBOL = "AAPL";

    @Test
    public void testGetStockNews() throws Exception {
        List<Article> articles = List.of(
                new Article(new Source("1", "source1"),
                        "author1",
                        "title1",
                        "description1",
                        "url1",
                        "urlToImage1",
                        "dateTime1",
                        "content1"),
                new Article(new Source("2", "source2"),
                        "author2",
                        "title2",
                        "description2",
                        "url2",
                        "urlToImage2",
                        "dateTime2",
                        "content2")
        );

        NewsApiResponse mockNewsApiResponse = new NewsApiResponse("200", 2, articles);

        given(newsServiceImpl.processNews(SYMBOL)).willReturn(mockNewsApiResponse.articles());

        ResultActions response = mockMvc.perform(get("/news/" + SYMBOL));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$[0].title").value("title1"))
                .andExpect(jsonPath("$[1].source.name").value("source2"));
    }

    @Test
    public void testGetStockNewsReturnsEmptyList() throws Exception {
        given(newsServiceImpl.processNews(SYMBOL)).willReturn(Collections.emptyList());

        mockMvc.perform(get("/news/" + SYMBOL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    public void testGetStockNewsThrowsIllegalStateException() throws Exception {
        given(newsServiceImpl.processNews("InvalidSymbol")).willThrow(new IllegalStateException("Invalid symbol"));

        mockMvc.perform(get("/news/InvalidSymbol"))
                .andExpect(status().isBadRequest());
    }

}
