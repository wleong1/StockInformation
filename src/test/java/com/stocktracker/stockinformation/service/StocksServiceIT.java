package com.stocktracker.stockinformation.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.stocktracker.stockinformation.model.Company;
import com.stocktracker.stockinformation.model.Stock;
import com.stocktracker.stockinformation.repository.CompanyRepository;
import com.stocktracker.stockinformation.repository.StocksRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "alphavantageapi.url=http://localhost:8090"
})
@EnableWireMock(
        @ConfigureWireMock(
                port=8090
        ))
public class StocksServiceIT {

    @Autowired
    StocksRepository stocksRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    StocksService stocksService;

    @BeforeEach
    void setup() {
        stocksRepository.deleteAll();
        companyRepository.deleteAll();
    }

    @Test
    void updateAllStocks_shouldSaveStock() {

        Company company = new Company();
        company.setTicker("AAPL");
        company.setCompanyName("Apple");
        companyRepository.save(company);

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

        stocksService.updateAllStocks();

        List<Stock> stocks = stocksRepository.findAll();
        assertFalse(stocks.isEmpty());

        Stock stock = stocks.get(0);
        assertEquals("AAPL", stock.getCompany().getTicker());
        assertNotNull(stock.getTradeDate());
    }

    @Test
    public void getClosePriceInvalidTicker_shouldReturnEmpty() {

        Company company = new Company();
        company.setTicker("INVALIDTICKER");
        company.setCompanyName("InvalidCompany");
        companyRepository.save(company);

        WireMock.stubFor(WireMock.get(urlPathEqualTo("/query"))
                .withQueryParam("function", equalTo("TIME_SERIES_DAILY"))
                .withQueryParam("symbol", equalTo("INVALIDCOMPANY"))
                .withQueryParam("apikey", matching(".*"))
                .withQueryParam("outputsize", equalTo("compact"))
                .withQueryParam("datatype", equalTo("json"))
                .willReturn(badRequest()));

        stocksService.updateAllStocks();

        List<Stock> stocks = stocksRepository.findAll();
        assertTrue(stocks.isEmpty(), "Data not found");
    }
}
