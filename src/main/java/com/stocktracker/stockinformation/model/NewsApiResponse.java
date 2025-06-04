package com.stocktracker.stockinformation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public record NewsApiResponse(

        @JsonIgnore
        String status,

        int totalResults,

        List<Article> articles
) {}