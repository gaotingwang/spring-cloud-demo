package com.gtw.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DiscoveryController {
    @Autowired
    DiscoveryClient discoveryClient;

    @GetMapping("/hello")
    public String index(@RequestParam String name) {
        String services = "Services: " + discoveryClient.getServices();
        System.out.println(services);
        return "hello " + name + "，" + services;
    }
}
