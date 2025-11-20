package com.group7.DMS.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.base-dir:}")
    private String uploadBaseDir;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseDir;
        if (uploadBaseDir != null && !uploadBaseDir.isEmpty()) {
            baseDir = uploadBaseDir;
        } else {
            // Default to project root if not configured
            baseDir = System.getProperty("user.dir");
        }
        
        // Serve uploaded files from uploads directory
        String uploadPath = Paths.get(baseDir, "uploads").toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}

