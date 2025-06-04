package com.stocktracker.stockinformation.controller;


import com.stocktracker.stockinformation.model.DailyQuote;
import com.stocktracker.stockinformation.service.PriceServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceController.class)
public class PriceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PriceServiceImpl priceServiceImpl;

    private final String SYMBOL = "AAPL";

    @Test
    public void testGetStockData() throws Exception {
        DailyQuote dailyQuote = new DailyQuote("openPrice", "highPrice", "lowPrice", "closePrice", "volume");


        given(priceServiceImpl.getLatestClosingPrice(SYMBOL)).willReturn(dailyQuote.close());

        ResultActions response = mockMvc.perform(get("/price/" + SYMBOL));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string(dailyQuote.close()));
    }

    @Test
    public void testGetStockData_NoData_ShouldReturnBadRequest() throws Exception {
        given(priceServiceImpl.getLatestClosingPrice("COMPANYWITHNODATA"))
                .willThrow(new IllegalStateException("No closing price found"));

        mockMvc.perform(get("/price/COMPANYWITHNODATA"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("No closing price found")));
    }

    @Test
    public void testGetStockData_InvalidSymbol_ShouldReturnBadRequest() throws Exception {
        given(priceServiceImpl.getLatestClosingPrice("INVALIDCOMPANY"))
                .willThrow(new IllegalStateException("No time series data found"));

        mockMvc.perform(get("/price/INVALIDCOMPANY"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("No time series data found")));
    }
}
