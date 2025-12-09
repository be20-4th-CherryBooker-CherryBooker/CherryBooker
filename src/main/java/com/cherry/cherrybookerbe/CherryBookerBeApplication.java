package com.cherry.cherrybookerbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class CherryBookerBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CherryBookerBeApplication.class, args);
    }

}
