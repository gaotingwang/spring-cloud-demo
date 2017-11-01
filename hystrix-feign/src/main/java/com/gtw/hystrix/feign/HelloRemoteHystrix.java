package com.gtw.hystrix.feign;

import org.springframework.stereotype.Component;

@Component
public class HelloRemoteHystrix implements HelloRemote {
    @Override
    public String hello(String name) {
        return "message send failed ";
    }
}
