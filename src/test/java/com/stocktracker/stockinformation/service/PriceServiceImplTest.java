package com.stocktracker.stockinformation.service;


import com.stocktracker.stockinformation.model.AlphaVantageApiResponse;
import com.stocktracker.stockinformation.model.DailyQuote;
import com.stocktracker.stockinformation.model.MetaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PriceServiceImplTest {

    @Mock
    private RestClient mockPriceApiClient;

    @Mock
    private RestClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    @InjectMocks
    private PriceServiceImpl priceService;

    private final String API_KEY = "dummy-api-key";
    private final String SYMBOL = "AAPL";

    @BeforeEach
    void setUp() {
        priceService = new PriceServiceImpl(mockPriceApiClient, API_KEY);
    }

    @Test
    void testGetLatestClosingPrice_shouldBeSuccessful() {
        DailyQuote mockDailyQuote = new DailyQuote("openPrice", "highPrice", "lowPrice", "closePrice", "volume");
        AlphaVantageApiResponse mockAlphaVantageApiResponse = new AlphaVantageApiResponse(new MetaData("metadata1",
                "metadata2",
                "metadata3",
                "metadata4",
                "metadata5"),
                Map.of("2025-05-20", mockDailyQuote));

        when(mockPriceApiClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(AlphaVantageApiResponse.class)).thenReturn(mockAlphaVantageApiResponse);

        String result = priceService.getLatestClosingPrice(SYMBOL);

        assertEquals("closePrice", result);

    }

    @Test
    void testNoTimeSeries_shouldThrowIllegalStateException() {
        AlphaVantageApiResponse mockAlphaVantageApiResponse = new AlphaVantageApiResponse(
                new MetaData("m1", "m2", "m3", "m4", "m5"),
                null
        );

        when(mockPriceApiClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(AlphaVantageApiResponse.class)).thenReturn(mockAlphaVantageApiResponse);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> priceService.getLatestClosingPrice(SYMBOL));

        assertEquals("No time series data found", exception.getMessage());
    }

    @Test
    void testFetchTimeSeries_shouldThrowBadRequest() {
        when(mockPriceApiClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenThrow(
                new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request"));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> priceService.getLatestClosingPrice(SYMBOL));

        assertTrue(exception.getMessage().contains("Bad Request"));
    }

    @Test
    void testLatestClosingPrice_shouldThrowIllegalStateException() {
        DailyQuote incompleteQuote = new DailyQuote("open", "high", "low", null, "volume");
        AlphaVantageApiResponse response = new AlphaVantageApiResponse(
                new MetaData("m1", "m2", "m3", "m4", "m5"),
                Map.of("2025-05-20", incompleteQuote)
        );

        when(mockPriceApiClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(AlphaVantageApiResponse.class)).thenReturn(response);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                priceService.getLatestClosingPrice(SYMBOL)
        );

        assertEquals("No closing price found", exception.getMessage());
    }
}
