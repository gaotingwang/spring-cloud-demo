package com.gtw.hystrix;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;

// 相当于@SpringBootApplication + @EnableDiscoveryClient + @EnableCircuitBreaker。意味着一个Spring Cloud标准应用应包含服务发现以及断路器。
@SpringCloudApplication
public class ConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConsumerApplication.class, args);
    }
}
