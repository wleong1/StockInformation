package com.stocktracker.stockinformation.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MetaData(
        @JsonProperty("1. Information") String information,
        @JsonProperty("2. Symbol") String symbol,
        @JsonProperty("3. Last Refreshed") String lastRefreshed,
        @JsonProperty("4. Output Size") String outputSize,
        @JsonProperty("5. Time Zone") String timeZone
) {}
