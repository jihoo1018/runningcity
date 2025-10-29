package com.runningcity.global.common.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry reg) {
        reg.addMapping("/**")
           .allowedOrigins("*")
           .allowedMethods("GET","POST","PATCH","PUT","DELETE","OPTIONS")
           .allowedHeaders("*")
//          .allowCredentials(true)
           .maxAge(3600);
    }
}
