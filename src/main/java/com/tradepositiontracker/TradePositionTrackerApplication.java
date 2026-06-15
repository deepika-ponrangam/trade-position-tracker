package com.tradepositiontracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
@SpringBootApplication
@EnableJpaAuditing
public class TradePositionTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradePositionTrackerApplication.class, args);
    }
}
