package com.gtw.eureka.consumer.normal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class NormalController {
    @Autowired
    LoadBalancerClient loadBalancerClient;

    @Autowired
    @Qualifier("normalRestTemplate")
    RestTemplate restTemplate;

    @GetMapping("/normal")
    public String test() {
        // 负载均衡的选出一个eureka-producer的服务实例，这个服务实例的基本信息存储在ServiceInstance中
        ServiceInstance serviceInstance = loadBalancerClient.choose("eureka-producer");
        String url = "http://" + serviceInstance.getHost() + ":" + serviceInstance.getPort() + "/hello?name=Alice";
        System.out.println(url);
        return restTemplate.getForObject(url, String.class);
    }
}
