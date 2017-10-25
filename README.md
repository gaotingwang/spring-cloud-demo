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
       <!--服务器发现者server部分-->
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