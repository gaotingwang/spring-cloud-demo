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

## [Eureka Server](https://github.com/gaotingwang/spring-cloud-demo/tree/master/eureka-server)

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

启动工程后，可以通过访问[http://localhost:8000/](http://localhost:8000/)查看服务注册中心。

### 高可用

Eureka Server除了单点运行之外，还可以通过运行多个实例，并进行互相注册的方式来实现高可用的部署，所以我们只需要将Eureke Server配置其他可用的serviceUrl就能实现高可用部署。Eureka Server的同步遵循着一个非常简单的原则：只要有一条边将节点连接，就可以进行信息传播与同步。假设我们有3个注册中心，我们将peer1、peer2、peer3各自都将serviceUrl指向另外两个节点。换言之，peer1、peer2、peer3是两两互相注册的。启动三个服务注册中心，并将compute-service的serviceUrl指向peer1并启动，可以获得如下图所示的集群效果。

![eureka-server集群](http://blog.didispace.com/content/images/2016/09/s1.png)

修改配置文件：

```yaml
spring:
  application:
    name: eureka-server-register-center

---
spring:
  profiles: register-center1
server:
  port: 8000
eureka:
  instance:
    hostname: register-center1
  client:
    service-url:
      defaultZone: http://register-center2:8001/eureka/,http://register-center3:8002/eureka/
---
spring:
  profiles: register-center2
server:
  port: 8001
eureka:
  instance:
    hostname: register-center2
  client:
    service-url:
      defaultZone: http://register-center1:8000/eureka/,http://register-center3:8002/eureka/
---
spring:
  profiles: register-center3
server:
  port: 8002
eureka:
  instance:
    hostname: register-center3
  client:
    service-url:
      defaultZone: http://register-center1:8000/eureka/,http://register-center2:8001/eureka/
```

修改host文件，在`/etc/hosts`文件中添加对 register-center的映射转换:

```
127.0.0.1 register-center1
127.0.0.1 register-center2
127.0.0.1 register-center3
```

通过`spring.profiles.active`属性来分别启动:

```shell
java -jar target/eureka-server-1.0-SNAPSHOT.jar --spring.profiles.active=register-center1
java -jar target/eureka-server-1.0-SNAPSHOT.jar --spring.profiles.active=register-center2
java -jar target/eureka-server-1.0-SNAPSHOT.jar --spring.profiles.active=register-center3
```

## Eureka Client

上一步是服务注册中心的开发，现在是服务的提供与调用。流程是首先启动注册中心，服务提供者生产服务并注册到服务中心中，消费者从服务中心中获取服务并执行。

### [服务提供](https://github.com/gaotingwang/spring-cloud-demo/tree/master/eureka-producer)

1. 添加依赖：

   ```xml
   <!--服务发现者管理者，其内已经包括了spring-boot-starter-web和spring-cloud-starter-->
   <dependency>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-starter-eureka</artifactId>
   </dependency>
   ```

2. 在`Config`类中添加`@EnableDiscoveryClient`用来将当前应用加入到服务治理体系中

3. 修改配置文件`application.yml`，指定eureka注册中心的地址：

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

4. 提供服务

   ```java
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
   ```

添加`@EnableDiscoveryClient`注解后，项目就具有了服务注册的功能。启动工程后，就可以在注册中心的页面看到eureka-producer服务。

### [服务调用](https://github.com/gaotingwang/spring-cloud-demo/tree/master/eureka-consumer)

服务调用都需要依赖：

```xml
<!--服务发现者管理者，其内已经包括了spring-boot-starter-web和spring-cloud-starter-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```

- 普通方式，[示例](https://github.com/gaotingwang/spring-cloud-demo/tree/master/eureka-consumer/src/main/java/com/gtw/eureka/consumer/normal)

  ```java
  @Bean
  public RestTemplate normalRestTemplate() {
  	return new RestTemplate();
  }


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
  ```

- Ribbon，[示例](https://github.com/gaotingwang/spring-cloud-demo/tree/master/eureka-consumer/src/main/java/com/gtw/eureka/consumer/ribbon)

  1. 添加依赖：

     ```xml
     <!--添加Ribbon依赖-->
     <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-ribbon</artifactId>
     </dependency>
     ```

  2. 为`RestTemplate`增加`@LoadBalanced`注解：

     ```java
     @Configuration
     public class Ribbon {
         @Bean
         @LoadBalanced
         public RestTemplate ribbonRestTemplate() {
             return new RestTemplate();
         }
     }
     ```

  3. 调用服务提供者中的方法：

     ```java
     @RestController
     public class RibbonController {
         @Autowired
         private RestTemplate ribbonRestTemplate;

         /**
          * 请求的host位置并没有使用一个具体的IP地址和端口的形式，而是采用了服务名的方式组成。
          * 那么这样的请求为什么可以调用成功呢？因为Spring Cloud Ribbon有一个拦截器，
          * 它能够在这里进行实际调用的时候，自动的去选取服务实例，并将实际要请求的IP地址和端口替换这里的服务名，从而完成服务接口的调用。
          */
         @GetMapping("/consumer")
         public String testRibbon() {
             return ribbonRestTemplate.getForObject("http://eureka-producer/hello?name=Jack", String.class);
         }
     }
     ```

- Feign，[示例](https://github.com/gaotingwang/spring-cloud-demo/tree/master/eureka-consumer/src/main/java/com/gtw/eureka/consumer/feign)

  1. 添加依赖，并使用`@EnableFeignClients` 来启用feign进行远程调用,开启扫描Spring Cloud Feign客户端的功能

     ```xml
     <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-feign</artifactId>
     </dependency>
     ```

  2. 调用Fegin实现

     ```java
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
     ```

  通过Spring Cloud Feign来实现服务调用的方式更加简单了，通过`@FeignClient`定义的接口来统一的生命我们需要依赖的微服务接口。而在具体使用的时候就跟调用本地方法一点的进行调用即可。由于Feign是基于Ribbon实现的，所以它自带了客户端负载均衡功能，也可以通过Ribbon的IRule进行策略扩展。另外，Feign还整合的Hystrix来实现服务的容错保护，在Dalston版本中，Feign的Hystrix默认是关闭的。


## Hystrix

### 服务降级

这里的例子都是对[服务调用](https://github.com/gaotingwang/spring-cloud-demo/tree/master/eureka-consumer)的例子中进行改造，当请求服务提供者超时或者出错时，转为调用指定方法，防止出现因“服务提供者”的不可用导致“服务消费者”的不可用的雪崩效应。

- [Ribbon](https://github.com/gaotingwang/spring-cloud-demo/tree/master/hystrix-ribbon)

  1. 添加Hystrix依赖：

     ```xml
     <!--添加hystrix依赖-->
     <dependency>
         <groupId>org.springframework.cloud</groupId>
         <artifactId>spring-cloud-starter-hystrix</artifactId>
     </dependency>
     ```

  2. 启动类中使用`@SpringCloudApplication`(即使用 `@EnableDiscoveryClient` 、 `@EnableCircuitBreaker`)注解

     ```java
     // 相当于@SpringBootApplication + @EnableDiscoveryClient + @EnableCircuitBreaker。意味着一个Spring Cloud标准应用应包含服务发现以及断路器。
     @SpringCloudApplication
     public class ConsumerApplication {
         public static void main(String[] args) {
             SpringApplication.run(ConsumerApplication.class, args);
         }
     }
     ```

  3. 使用`@HystrixCommand`注解来指定服务降级方法

     ```java
     @Service
     class ConsumerService {
         @Autowired
         private RestTemplate ribbonRestTemplate;

         /**
          * 使用@HystrixCommand注解来指定服务降级方法,当方法请求超时或者请求错误，转为调用指定的降级方法
          */
         @HystrixCommand(fallbackMethod = "fallback")
         public String consumer() {
             return ribbonRestTemplate.getForObject("http://eureka-producer/hello?name=Jack", String.class);
         }

     	// 此方法为指定的降级调用的方法
         private String fallback() {
             return "fallback";
         }
     }
     ```

- [Feign](https://github.com/gaotingwang/spring-cloud-demo/tree/master/hystrix-feign)

  1. 开启feign降级支持

     ```yaml
     feign:
       hystrix:
         enabled: true
     ```

  2. 创建回调类，实现回调的方法：

     ```java
     @Component
     public class HelloRemoteHystrix implements HelloRemote {
         @Override
         public String hello(String name) {
             return "message send failed ";
         }
     }
     ```

  3. 在`@FeignClient`中添加指定fallback类，在服务熔断的时候返回fallback类中的内容。

     ```java
     @FeignClient(name= "eureka-producer", fallback = HelloRemoteHystrix.class)
     public interface HelloRemote {

         /**
          * 此类中的请求路径和远程服务中Controller中的请求路径和参数需保持一致
          */
         @GetMapping(value = "/hello")
         String hello(@RequestParam(value = "name") String name);
     }
     ```