package com.stocktracker.stockinformation.service;

import com.stocktracker.stockinformation.model.DailyQuote;

import java.util.Map;

public interface PriceService {

    String getLatestClosingPrice(String symbol);

    Map<String, DailyQuote> getDailyTimeSeries(String ticker);
}
