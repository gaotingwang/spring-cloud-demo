# spring-cloud-demo
因为spring-cloud是依赖于spring-boot，所以项目在创建时需要按照spring-boot创建方式创建，首先是在pom.xml中添加spring-boot作为parent ：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>1.5.6.RELEASE</version>
</parent>
```

需要注意spring-boot版本与spring-cloud版本关系，参考[官网spring-cloud](https://projects.spring.io/spring-cloud/)，底部有说明。

接下来需要引入spring-cloud的依赖，因为pom也是单继承，所以通过`<dependencyManagement>`的`<scope>import</scope>`方式引入spring-cloud依赖:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>Dalston.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## Eureka Server

spring cloud已经实现了服务注册中心，我们只需要很简单的几个步骤就可以完成。

1. 添加pom依赖：

   ```xml
   <dependencies>
       <!--服务发现者server部分-->
       <dependency>
           <groupId>org.springframework.cloud</groupId>
           <artifactId>spring-cloud-starter-eureka-server</artifactId>
       </dependency>
   </dependencies>
   ```

2. 在`Config`类中添加`@EnableEurekaServer`来启动一个服务注册中心提供给其他应用进行对话

3. 修改配置

   ```yaml
   spring:
     application:
       name: eureka-server-register-center
   server:
     port: 8000 # 为了与后续要进行注册的服务区分，这里将服务注册中心的端口通过server.port属性设置为8000
   eureka:
     instance:
       hostname: localhost
     client:
       register-with-eureka: false # 表示是否将自己注册到Eureka Server，默认为true。默认情况下服务注册中心也会将自己作为客户端来尝试注册它自己，所以需要禁用它的客户端注册行为
       fetch-registry: false # 表示是否从Eureka Server获取注册信息，默认为true。
       service-url:
         defaultZone: http://localhost:${server.port}/eureka/ # 设置与Eureka Server交互的地址，查询服务和注册服务都需要依赖这个地址。
   ```

启动工程后，可以通过访问[http://localhost:8000/](http://localhost:8000/)查看服务注册中心

## Eureka Client

上一步是服务注册中心的开发，现在是服务的提供与调用。流程是首先启动注册中心，服务提供者生产服务并注册到服务中心中，消费者从服务中心中获取服务并执行。

添加依赖：

```xml
<!--服务发现者管理者，其内已经包括了spring-boot-starter-web和spring-cloud-starter-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```

### 服务提供

1. 在`Config`类中添加`@EnableDiscoveryClient`用来将当前应用加入到服务治理体系中

2. 修改配置文件`application.yml`，指定eureka注册中心的地址：

   ```yaml
   spring:
     application:
       name: eureka-producer
   server:
     port: 9000
   eureka:
     client:
       service-url:
         defaultZone: http://localhost:8000/eureka/ #对应服务注册中心的配置内容，指定服务注册中心的位置。
   ```

3. 提供服务

   ```java
   @RestController
   public class DiscoveryController {
       @Autowired
       DiscoveryClient discoveryClient;

       @GetMapping("/discovery")
       public String dc() {
           String services = "Services: " + discoveryClient.getServices();
           System.out.println(services);
           return services;
       }
   }
   ```

添加`@EnableDiscoveryClient`注解后，项目就具有了服务注册的功能。启动工程后，就可以在注册中心的页面看到eureka-producer服务。

### 服务调用

- Feign

  通过Spring Cloud Feign来实现服务调用的方式更加简单了，通过`@FeignClient`定义的接口来统一的生命我们需要依赖的微服务接口。而在具体使用的时候就跟调用本地方法一点的进行调用即可。由于Feign是基于Ribbon实现的，所以它自带了客户端负载均衡功能，也可以通过Ribbon的IRule进行策略扩展。另外，Feign还整合的Hystrix来实现服务的容错保护，在Dalston版本中，Feign的Hystrix默认是关闭的。

- ​

