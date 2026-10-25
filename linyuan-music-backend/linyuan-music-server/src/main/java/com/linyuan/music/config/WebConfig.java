package com.linyuan.music.config;

import com.linyuan.music.interceptor.LoginInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;

    @Value("${linyuan-music.storage.local-dir:./uploads}")
    private String localStorageDir;

    @Value("${linyuan-music.storage.url-prefix:/files}")
    private String storageUrlPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录接口和注册接口不拦截
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/admin/login", "/admin/logout", "/admin/register",
                        "/user/login", "/user/logout", "/user/register",
                        "/user/sendVerificationCode", "/user/resetUserPassword",
                        "/banner/getBannerList",
                        "/playlist/getAllPlaylists", "/playlist/getRecommendedPlaylists", "/playlist/getPlaylistDetail/**",
                        "/artist/getAllArtists", "/artist/getArtistDetail/**",
                        "/song/getAllSongs", "/song/getRecommendedSongs", "/song/getSongDetail/**",
                        // 本地存储的静态资源（封面、音频）无需登录即可访问
                        storageUrlPrefix + "/**");
    }

    /**
     * 本地磁盘存储的静态资源映射：/files/** -> ${linyuan-music.storage.local-dir}
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(localStorageDir).toAbsolutePath().normalize();
        String location = dir.toUri().toString();
        registry.addResourceHandler(storageUrlPrefix + "/**")
                .addResourceLocations(location);
        log.info("本地文件映射: {}/** -> {}", storageUrlPrefix, location);
    }
}
