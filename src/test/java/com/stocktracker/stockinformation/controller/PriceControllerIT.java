package com.stocktracker.stockinformation.controller;


import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "alphavantageapi.url=http://localhost:8089"
})
@EnableWireMock(
        @ConfigureWireMock(
                port=8089
))
public class PriceControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getStockPrice_withAAPL_shouldReturnValid() throws Exception {
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("TIME_SERIES_DAILY"))
                .withQueryParam("symbol", equalTo("AAPL"))
                .withQueryParam("apikey", matching(".*"))
                .withQueryParam("outputsize", equalTo("compact"))
                .withQueryParam("datatype", equalTo("json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                        {
                          "Meta Data": {
                            "1. Information": "Daily Prices (open, high, low, close) and Volumes",
                            "2. Symbol": "AAPL",
                            "3. Last Refreshed": "2025-06-03",
                            "4. Output Size": "Compact",
                            "5. Time Zone": "US/Eastern"
                          },
                          "Time Series (Daily)": {
                            "2025-06-03": {
                              "1. open": "191.87",
                              "2. high": "194.90",
                              "3. low": "190.30",
                              "4. close": "194.10",
                              "5. volume": "56789321"
                            },
                            "2025-06-02": {
                              "1. open": "189.52",
                              "2. high": "192.30",
                              "3. low": "188.40",
                              "4. close": "191.80",
                              "5. volume": "48123456"
                            }
                          }
                        }
                        """)));
        mockMvc.perform(get("/price/AAPL"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void getStockPrice_withInvalidCompany_shouldThrowException() throws Exception {
        WireMock.stubFor(WireMock.get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("TIME_SERIES_DAILY"))
                .withQueryParam("symbol", equalTo("INVALIDCOMPANY"))
                .withQueryParam("apikey", matching(".*"))
                .withQueryParam("outputsize", equalTo("compact"))
                .withQueryParam("datatype", equalTo("json"))
                .willReturn(badRequest()));
        mockMvc.perform(get("/price/INVALIDCOMPANY"))
                .andExpect(status().isBadRequest());
    }

}
