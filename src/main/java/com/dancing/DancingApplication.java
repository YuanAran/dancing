package com.dancing;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dancing.mapper")
public class DancingApplication {

    public static void main(String[] args) {
        SpringApplication.run(DancingApplication.class, args);
    }

}
