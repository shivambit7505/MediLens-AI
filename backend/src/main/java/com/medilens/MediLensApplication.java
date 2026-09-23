package com.medilens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MediLensApplication {

    public static void main(String[] args) {
        SpringApplication.run(MediLensApplication.class, args);
    }
}
