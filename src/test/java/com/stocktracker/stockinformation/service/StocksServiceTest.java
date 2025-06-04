package com.stocktracker.stockinformation.service;


import com.stocktracker.stockinformation.model.Company;
import com.stocktracker.stockinformation.model.DailyQuote;
import com.stocktracker.stockinformation.model.Stock;
import com.stocktracker.stockinformation.repository.CompanyRepository;
import com.stocktracker.stockinformation.repository.StocksRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StocksServiceTest {

    @InjectMocks
    private StocksServiceImpl stocksService;

    @Mock
    private StocksRepository stocksRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private PriceService priceService;

    @Test
    void updateAllStocks_shouldBeSuccessful() {

        String ticker = "AAPL";

        Company mockCompany = new Company();
        mockCompany.setTicker(ticker);
        mockCompany.setCompanyName("APPLE");

        when(companyRepository.findAll()).thenReturn(List.of(mockCompany));

        DailyQuote mockDailyQuote = new DailyQuote("120.00", "122.00", "118.00", "120.02", "2000");
        when(priceService.getDailyTimeSeries(ticker)).thenReturn(Map.of("2024-02-29", mockDailyQuote));

        Stock mockStock = new Stock();
        mockStock.setCompany(mockCompany);
        mockStock.setTradeDate(LocalDate.of(2024, 2, 29));
        mockStock.setOpen(new BigDecimal("120.00"));
        mockStock.setHigh(new BigDecimal("122.00"));
        mockStock.setLow(new BigDecimal("118.00"));
        mockStock.setClose(new BigDecimal("120.02"));
        mockStock.setVolume(new BigInteger("2000"));

        when(stocksRepository.save(mockStock)).thenReturn(mockStock);

        stocksService.updateAllStocks();

        verify(companyRepository, times(1)).findAll();
        verify(priceService, times(1)).getDailyTimeSeries(ticker);
        verify(stocksRepository, times(1)).save(mockStock);
    }

    @Test
    void updateAllStocks_shouldReturnEntityNotFoundException() {

        String ticker = "AAPL";

        Company mockCompany = new Company();
        mockCompany.setTicker(ticker);
        mockCompany.setCompanyName("APPLE");

        when(companyRepository.findAll()).thenReturn(List.of(mockCompany));

        when(priceService.getDailyTimeSeries(ticker)).thenThrow(new EntityNotFoundException("Data not found"));

        stocksService.updateAllStocks();
        verify(stocksRepository, never()).save(any());
    }

}
