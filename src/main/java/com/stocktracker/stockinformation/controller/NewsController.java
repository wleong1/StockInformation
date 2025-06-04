package com.stocktracker.stockinformation.controller;

import com.stocktracker.stockinformation.model.Article;
import com.stocktracker.stockinformation.service.NewsServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/news")
public class NewsController {

    private final NewsServiceImpl newsServiceImpl;

    public NewsController(NewsServiceImpl newsServiceImpl) {
        this.newsServiceImpl = newsServiceImpl;
    }

    @GetMapping("/{company}")
    public List<Article> getNews(@PathVariable String company) {
        return newsServiceImpl.processNews(company);
    }

}
