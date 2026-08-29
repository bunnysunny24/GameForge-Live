package com.gameforge.live;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GameForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameForgeApplication.class, args);
    }
}
