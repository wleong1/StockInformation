package com.stocktracker.stockinformation.service;

import com.stocktracker.stockinformation.exception.ApiException;
import com.stocktracker.stockinformation.model.AlphaVantageApiResponse;
import com.stocktracker.stockinformation.model.DailyQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Slf4j
@Service
public class PriceServiceImpl implements PriceService {

    private final RestClient restClient;
    private final String apiKey;

    public PriceServiceImpl(RestClient alphaVantageApiClient,
                            @Value("${alphavantageapi.key}") String apiKey) {
        this.restClient = alphaVantageApiClient;
        this.apiKey = apiKey;
    }

    private String generateUriString(String symbol) {
        return UriComponentsBuilder.newInstance()
                .path("/query")
                .queryParam("function", "TIME_SERIES_DAILY")
                .queryParam("symbol", symbol)
                .queryParam("apikey", apiKey)
                .queryParam("outputsize", "compact")
                .queryParam("datatype", "json")
                .build()
                .toUriString();
    }

    private AlphaVantageApiResponse fetchTimeSeries(String symbol) {
        String uri = generateUriString(symbol);
        try {
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(AlphaVantageApiResponse.class);
        } catch (HttpServerErrorException httpEx) {
            log.error("Server error: " + httpEx.getMessage() + httpEx + httpEx.getStatusCode());
            throw new ApiException("AlphaVantage server error: " + httpEx.getMessage());
        } catch (HttpClientErrorException httpEx) {
            log.error("Client error: " + httpEx.getMessage() + httpEx + httpEx.getStatusCode());
            throw new ResponseStatusException(httpEx.getStatusCode(), httpEx.getMessage(), httpEx);
        }
    }

    public Map<String, DailyQuote> getDailyTimeSeries(String symbol) {
        AlphaVantageApiResponse response = fetchTimeSeries(symbol);
        Map<String, DailyQuote> series = response.timeSeriesDaily();
        if (series == null || series.isEmpty()) {
            throw new IllegalStateException("No time series data found");
        }
        return series;
    }

    public String getLatestClosingPrice(String symbol) {
        return getDailyTimeSeries(symbol).entrySet().stream()
                .max(Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().close())
                .orElseThrow(() -> new IllegalStateException(("No closing price found")));
    }

}
