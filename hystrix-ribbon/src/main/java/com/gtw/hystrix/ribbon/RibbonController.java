package com.gtw.hystrix.ribbon;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class RibbonController {

    @Autowired
    ConsumerService consumerService;

    @GetMapping("/consumer")
    public String testRibbon() {
        return consumerService.consumer();
    }

    @Service
    class ConsumerService {
        @Autowired
        private RestTemplate ribbonRestTemplate;

        /**
         * 使用@HystrixCommand注解来指定服务降级方法
         */
        @HystrixCommand(fallbackMethod = "fallback")
        public String consumer() {
            return ribbonRestTemplate.getForObject("http://eureka-producer/hello?name=Jack", String.class);
        }

        private String fallback() {
            return "fallback";
        }
    }
}
