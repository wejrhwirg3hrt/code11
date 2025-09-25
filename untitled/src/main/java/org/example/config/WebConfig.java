package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.path:E:/code11/uploads}")
    private String uploadPath;

    /**
     * 配置静态资源映射
     * 使上传的文件可以通过 /uploads/** 路径访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射上传目录到Web路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
        
        System.out.println("✅ 静态资源映射配置: /uploads/** -> " + uploadPath + "/");
    }
}