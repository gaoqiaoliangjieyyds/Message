package com.jia;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.jia.mapper")
@SpringBootApplication
public class WebSoketApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebSoketApplication.class, args);
    }

}
