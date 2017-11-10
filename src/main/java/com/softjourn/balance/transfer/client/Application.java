package com.softjourn.balance.transfer.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.softjourn.balance.transfer.client")
@EnableConfigurationProperties
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}



