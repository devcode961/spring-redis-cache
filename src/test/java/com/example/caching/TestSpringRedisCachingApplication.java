package com.example.caching;

import org.springframework.boot.SpringApplication;

public class TestSpringRedisCachingApplication {

    public static void main(String[] args) {
        SpringApplication.from(SpringRedisCachingApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
