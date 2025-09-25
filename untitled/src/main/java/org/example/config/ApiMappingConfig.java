package org.example.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * API映射配置 - 确保API控制器映射优先级高于静态资源处理器
 */
@Configuration
public class ApiMappingConfig {

    @Bean
    public WebMvcRegistrations webMvcRegistrations() {
        return new WebMvcRegistrations() {
            @Override
            public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
                RequestMappingHandlerMapping mapping = new RequestMappingHandlerMapping();
                // 设置最高优先级，确保API映射优先于静态资源处理器
                mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
                return mapping;
            }
        };
    }
}
