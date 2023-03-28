package com.example;

import com.dk0124.cdr.constants.coinCode.UpbitCoinCode.UpbitCoinCode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class,args);
        System.out.println("HELLO WORLD!");
    }
}
