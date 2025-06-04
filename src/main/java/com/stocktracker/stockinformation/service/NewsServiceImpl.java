package com.stocktracker.stockinformation.service;

import com.stocktracker.stockinformation.exception.ApiException;
import com.stocktracker.stockinformation.model.Article;
import com.stocktracker.stockinformation.model.NewsApiResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
public class NewsServiceImpl implements NewsService {

    private final RestClient restClient;
    private final String apiKey;

    public NewsServiceImpl(RestClient newsApiClient,
                       @Value("${newsapi.key}") String apiKey) {
        this.restClient = newsApiClient;
        this.apiKey = apiKey;
    }

    private NewsApiResponse fetchNews(String symbol) {
        String uri = generateUriString(symbol);
        log.info("Calling News API with symbol: {}", symbol);
        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(NewsApiResponse.class);
    }

    private List<Article> extractArticles(NewsApiResponse response) {
        List<Article> articles = response.articles();
        if (articles == null || articles.isEmpty()) {
            throw new IllegalStateException("No articles found");
        }
        return articles;
    }

    private String generateUriString(String symbol) {
        return UriComponentsBuilder.newInstance()
                .path("/v2/everything")
                .queryParam("qInTitle", symbol)
                .queryParam("sortBy", "publishedAt")
                .queryParam("apiKey", apiKey)
                .build()
                .toUriString();
    }

    @Retry(name = "newsServiceInstance")
    @CircuitBreaker(name = "newsServiceInstance", fallbackMethod = "fallbackProcessNews")
    public List<Article> processNews(String symbol) {
        return extractArticles(fetchNews(symbol))
                .stream()
                .limit(5)
                .toList();
    }

    public List<Article> fallbackProcessNews(String symbol, Throwable t) {
        log.error("Circuit breaker fallback triggered by: {}", t.getClass().getName());
        if (t instanceof HttpClientErrorException httpEx) {
            log.error("News API client error: " + httpEx.getMessage() + httpEx + httpEx.getStatusCode());
            throw new ResponseStatusException(httpEx.getStatusCode(), httpEx.getMessage(), httpEx);
        } else if (t instanceof HttpServerErrorException httpEx) {
            log.error("Server error: " + httpEx.getMessage() + httpEx + httpEx.getStatusCode());
            throw new ApiException("News API server error: " + t.getMessage());
        } else {
            log.error("Other error: " + t.getMessage());
            throw new ApiException("Unexpected error" + t.getMessage());
        }
    }

}
