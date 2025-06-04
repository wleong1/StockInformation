package com.stocktracker.stockinformation.service;

import com.stocktracker.stockinformation.model.Company;
import com.stocktracker.stockinformation.model.DailyQuote;
import com.stocktracker.stockinformation.model.Stock;
import com.stocktracker.stockinformation.repository.CompanyRepository;
import com.stocktracker.stockinformation.repository.StocksRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class StocksServiceImpl implements StocksService{

    @Autowired
    StocksRepository stocksRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    PriceService priceService;

    private Stock createStock(
            Company company,
            LocalDate tradeDate,
            BigDecimal open,
            BigDecimal high,
            BigDecimal low,
            BigDecimal close,
            BigInteger volume
    ) {
        Stock stock = new Stock();
        stock.setCompany(company);
        stock.setTradeDate(tradeDate);
        stock.setOpen(open);
        stock.setHigh(high);
        stock.setLow(low);
        stock.setClose(close);
        stock.setVolume(volume);

        return stock;
    }

//    @Scheduled(cron = "@daily")
    public void updateAllStocks() {
        List<Company> companies = companyRepository.findAll();

        for (Company company: companies) {
            try {
                Optional<Map.Entry<String, DailyQuote>> latestEntry = priceService.getDailyTimeSeries(company.getTicker())
                        .entrySet().stream().max(Map.Entry.comparingByKey());

                latestEntry.ifPresent(entry -> {
                    String date = entry.getKey();
                    DailyQuote quote = entry.getValue();
                    Stock stock = createStock(
                            company,
                            LocalDate.parse(date),
                            new BigDecimal(quote.open()),
                            new BigDecimal(quote.high()),
                            new BigDecimal(quote.low()),
                            new BigDecimal(quote.close()),
                            new BigInteger(quote.volume())
                    );
                    stocksRepository.save(stock);
                });
            } catch (Exception e) {
                log.error("Failed to update stock for {}: {}", company.getTicker(), e.getMessage());
            }
        }
    }

}
