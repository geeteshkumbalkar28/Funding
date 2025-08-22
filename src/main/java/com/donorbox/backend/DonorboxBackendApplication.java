package com.donorbox.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling 
public class DonorboxBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DonorboxBackendApplication.class, args);
    }

}
