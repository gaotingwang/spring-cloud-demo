package com.gtw.eureka.consumer.feign;

import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients // 启用feign进行远程调用,开启扫描Spring Cloud Feign客户端的功能
public class FeignConfig {
}
