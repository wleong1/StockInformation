package com.stocktracker.stockinformation.controller;

import com.stocktracker.stockinformation.service.PriceServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/price")
public class PriceController {

    private final PriceServiceImpl priceServiceImpl;

    public PriceController(PriceServiceImpl priceServiceImpl) {
        this.priceServiceImpl = priceServiceImpl;
    }

    @GetMapping("/{company}")
    public String getStockData(@PathVariable String company) {
        return priceServiceImpl.getLatestClosingPrice(company);
    }

}
