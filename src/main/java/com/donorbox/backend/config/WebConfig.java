package com.donorbox.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded images as static content
        registry.addResourceHandler("/api/images/**")//causes,event image access,blog
                .addResourceLocations("file:" + Paths.get(uploadDir).toAbsolutePath().toString() + "/")
                .setCachePeriod(3600); // Cache for 1 hour
    }
}
