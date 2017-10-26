package com.gtw.eureka.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient // 该注解能激活Eureka中的DiscoveryClient实现，这样才能实现Controller中对服务信息的输出。
public class EurekaProducerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaProducerApplication.class, args);
    }
}
