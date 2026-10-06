package com.nawaf.capstone3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync                               // ⚠ جديد: بدونه لا يعمل @Async

public class Capstone3Application {

    public static void main(String[] args) {
        SpringApplication.run(Capstone3Application.class, args);
    }

}