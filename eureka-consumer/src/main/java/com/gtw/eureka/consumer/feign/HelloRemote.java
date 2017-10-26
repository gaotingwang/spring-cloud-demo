package com.gtw.eureka.consumer.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign是一个声明式Web Service客户端。使用Feign能让编写Web Service客户端更加简单。
 * 它的使用方法是定义一个接口，然后在上面添加注解，同时也支持JAX-RS标准的注解。Feign也支持可拔插式的编码器和解码器。
 * Spring Cloud对Feign进行了封装，使其支持了Spring MVC标准注解和HttpMessageConverters。Feign可以与Eureka和Ribbon组合使用以支持负载均衡。
 */
@FeignClient(name= "eureka-producer") // name:远程服务名，即服务提供者的spring.application.name配置的名称
public interface HelloRemote {

    /**
     * 此类中的请求路径和远程服务中Controller中的请求路径和参数需保持一致
     */
    @RequestMapping(value = "/hello")
    String hello(@RequestParam(value = "name") String name);
}
