package com.example.sistemafinancas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registra o AuthInterceptor e define quais rotas são PÚBLICAS (não exigem login).
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor())
                .addPathPatterns("/**")          // Protege tudo por padrão
                .excludePathPatterns(            // Exceto as rotas públicas:
                        "/login",
                        "/login/**",
                        "/registrar",
                        "/registrar/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico"
                );
    }
}
