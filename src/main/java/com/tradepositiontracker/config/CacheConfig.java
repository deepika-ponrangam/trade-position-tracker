package com.tradepositiontracker.config;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    public static final String EXCHANGE_RATE_CACHE = "exchangeRates";
    @Bean
    public CacheManager cacheManager(){
        return new ConcurrentMapCacheManager(EXCHANGE_RATE_CACHE);
    }
}
