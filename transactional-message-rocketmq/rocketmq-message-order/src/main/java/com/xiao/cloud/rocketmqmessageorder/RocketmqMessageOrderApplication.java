package com.xiao.cloud.rocketmqmessageorder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@MapperScan("com.xiao.cloud.cloudcommon.message_tx.order.mapper")
public class RocketmqMessageOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(RocketmqMessageOrderApplication.class, args);
    }

}
