package com.example.caching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SpringRedisCachingApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringRedisCachingApplication.class, args);
    }

}
