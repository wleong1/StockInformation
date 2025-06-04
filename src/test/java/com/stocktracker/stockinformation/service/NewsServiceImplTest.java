package com.stocktracker.stockinformation.service;

import com.stocktracker.stockinformation.model.Article;
import com.stocktracker.stockinformation.model.NewsApiResponse;
import com.stocktracker.stockinformation.model.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NewsServiceImplTest {

    @Mock
    private RestClient mockNewsApiClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private NewsServiceImpl newsService;

    private final String API_KEY = "dummy-api-key";
    private final String SYMBOL = "AAPL";


    @BeforeEach
    void setUp() {
        newsService = new NewsServiceImpl(mockNewsApiClient, API_KEY);
    }

    @Test
    void testProcessNews_ReturnsLimitedArticles() {
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

        when(mockNewsApiClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NewsApiResponse.class)).thenReturn(mockNewsApiResponse);

        List<Article> result = newsService.processNews(SYMBOL);

        assertEquals(2, result.size());
        assertEquals("title1", result.get(0).title());
    }

    @Test
    void testProcessNews_ThrowsWhenNoArticles() {
        NewsApiResponse mockResponse = new NewsApiResponse("200", 0, Collections.emptyList());

        when(mockNewsApiClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NewsApiResponse.class)).thenReturn(mockResponse);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            newsService.processNews(SYMBOL);
        });

        assertEquals("No articles found", exception.getMessage());
    }

}
