package com.stocktracker.stockinformation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
//@EnableScheduling
public class StockInformationApplication {

	public static void main(String[] args) {
		SpringApplication.run(StockInformationApplication.class, args);
	}

}
