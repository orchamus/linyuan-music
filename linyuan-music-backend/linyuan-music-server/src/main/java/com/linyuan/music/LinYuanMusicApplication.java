package com.linyuan.music;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching   // 开启Spring Boot基于注解的缓存管理支持
@SpringBootApplication
public class LinYuanMusicApplication {

    public static void main(String[] args) {
        SpringApplication.run(LinYuanMusicApplication.class, args);
    }

}
