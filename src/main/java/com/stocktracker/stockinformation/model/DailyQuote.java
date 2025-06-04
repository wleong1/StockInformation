package com.stocktracker.stockinformation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DailyQuote(
        @JsonProperty("1. open") String open,
        @JsonProperty("2. high") String high,
        @JsonProperty("3. low") String low,
        @JsonProperty("4. close") String close,
        @JsonProperty("5. volume") String volume
) {}
