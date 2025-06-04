package com.stocktracker.stockinformation.controller;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.stocktracker.stockinformation.model.Article;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.List;
import java.util.stream.IntStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock(
        @ConfigureWireMock(
        port=8091
))
public class NewsControllerIT {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    NewsController newsController;

    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    public void setUp() {
        circuitBreakerRegistry.circuitBreaker("NewsService").reset();
    }

    @Test
    void testGetNews_returnsArticles() {
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/v2/everything"))
                .withQueryParam("qInTitle", equalTo("AAPL"))
                .withQueryParam("sortBy", equalTo("publishedAt"))
                .withQueryParam("apiKey", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                {
                  "status": "ok",
                  "totalResults": 1,
                  "articles": [
                    {
                      "source": {"id": null, "name": "Example.com"},
                      "author": "Author Name",
                      "title": "Example News Title",
                      "description": "Example description.",
                      "url": "https://example.com/article",
                      "urlToImage": "https://example.com/image.jpg",
                      "publishedAt": "2025-05-28T12:00:00Z",
                      "content": "Full content here."
                    }
                  ]
                }
                """)));

        List<Article> articles = newsController.getNews("AAPL");
        assert(!articles.isEmpty());
        assert(articles.get(0).title().equals("Example News Title"));
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
    }

    @Test
    void getStockNews_withInvalidCompany_shouldOpenCircuitBreaker() throws Exception {
//        CircuitBreaker.Metrics metrics = circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getMetrics();
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/v2/everything"))
                .withQueryParam("qInTitle", equalTo("INVALIDCOMPANY"))
                .withQueryParam("sortBy", equalTo("publishedAt"))
                .withQueryParam("apiKey", matching(".*"))
                .willReturn(serverError()));
        IntStream.range(0, 15).forEach(i -> {
            try {
                mockMvc.perform(get("/news/INVALIDCOMPANY"));
            } catch (Exception ignored) {}
        });

        assertEquals(CircuitBreaker.State.OPEN, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());

        Thread.sleep(1000);
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/v2/everything"))
                .withQueryParam("qInTitle", equalTo("AAPL"))
                .withQueryParam("sortBy", equalTo("publishedAt"))
                .withQueryParam("apiKey", matching(".*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                {
                  "status": "ok",
                  "totalResults": 1,
                  "articles": [
                    {
                      "source": {"id": null, "name": "Example.com"},
                      "author": "Author Name",
                      "title": "Example News Title",
                      "description": "Example description.",
                      "url": "https://example.com/article",
                      "urlToImage": "https://example.com/image.jpg",
                      "publishedAt": "2025-05-28T12:00:00Z",
                      "content": "Full content here."
                    }
                  ]
                }
                """)));

        try {
            mockMvc.perform(get("/news/AAPL"));
        } catch (Exception ignored) {}
        assertEquals(CircuitBreaker.State.HALF_OPEN, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());

        try {
            mockMvc.perform(get("/news/AAPL"));
        } catch (Exception ignored) {}
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
    }

    @Test
    void getStockNews_withInvalidCompany_shouldThrowForbidden() throws Exception {
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/v2/everything"))
                .withQueryParam("qInTitle", equalTo("FORBIDDENCOMPANY"))
                .withQueryParam("sortBy", equalTo("publishedAt"))
                .withQueryParam("apiKey", matching(".*"))
                .willReturn(forbidden()));
        mockMvc.perform(get("/news/FORBIDDENCOMPANY"))
                .andDo(print())
                .andExpect(status().isForbidden());

        // Have not reached minimum calls to trigger circuit breaker
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
    }

    @Test
    void getStockNews_withInvalidCompany_shouldThrowBadRequest() throws Exception {
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/v2/everything"))
                .withQueryParam("qInTitle", equalTo("BADREQUEST"))
                .withQueryParam("sortBy", equalTo("publishedAt"))
                .withQueryParam("apiKey", matching(".*"))
                .willReturn(badRequest()));
        mockMvc.perform(get("/news/BADREQUEST"))
                .andDo(print())
                .andExpect(status().isBadRequest());

        // Have not reached minimum calls to trigger circuit breaker
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
    }

    @Test
    void getStockNews_withInvalidCompany_shouldTriggerRetry() throws Exception {
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
        stubFor(WireMock.get(urlPathEqualTo("/v2/everything"))
                .withQueryParam("qInTitle", equalTo("INVALIDCOMPANY"))
                .withQueryParam("sortBy", equalTo("publishedAt"))
                .withQueryParam("apiKey", matching(".*"))
                .willReturn(serverError()));

        mockMvc.perform(get("/news/INVALIDCOMPANY"))
                .andExpect(status().isInternalServerError());

        WireMock.verify(3, getRequestedFor(urlPathEqualTo("/v2/everything")));


        // Have not reached minimum calls to trigger circuit breaker
        assertEquals(CircuitBreaker.State.CLOSED, circuitBreakerRegistry.circuitBreaker("newsServiceInstance").getState());
    }

}