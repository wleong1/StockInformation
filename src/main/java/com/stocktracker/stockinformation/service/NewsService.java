package com.stocktracker.stockinformation.service;

import com.stocktracker.stockinformation.model.Article;

import java.util.List;

public interface NewsService {

    List<Article> processNews(String symbol);
}
