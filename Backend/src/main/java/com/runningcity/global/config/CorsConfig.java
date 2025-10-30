package com.runningcity.global.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry reg) {
        reg.addMapping("/**")  // 모든 API 경로에 CORS 적용
//           .allowedOrigins("*") // allowedOrigins에서는 *(와일드카드) 사용 불가.
                .allowedOriginPatterns("*") // 모든 도메인에서 접근 허용 (개발용, 나중에 배포 시 도메인 제한 권장)
                .allowedMethods("GET","POST","PATCH","PUT","DELETE","OPTIONS") // 허용할 HTTP 메서드 지정
           .allowedHeaders("*")
//          .allowCredentials(true)
//          .exposedHeaders("Authorization")
           .maxAge(3600);
    }
}
