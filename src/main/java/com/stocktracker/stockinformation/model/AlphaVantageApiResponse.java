package com.stocktracker.stockinformation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record AlphaVantageApiResponse(
        @JsonProperty("Meta Data")
        MetaData metaData,

        @JsonProperty("Time Series (Daily)")
        Map<String, DailyQuote> timeSeriesDaily
) {}
