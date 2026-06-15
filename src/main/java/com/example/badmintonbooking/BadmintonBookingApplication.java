package com.example.badmintonbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class BadmintonBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BadmintonBookingApplication.class, args);
    }

}
