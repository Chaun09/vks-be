package vn.eledevo.vksbe.config;

import jakarta.servlet.MultipartConfigElement;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class FileUploadConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.parse("20GB")); // Giới hạn kích thước file
        factory.setMaxRequestSize(DataSize.parse("20GB")); // Giới hạn kích thước yêu cầu
        return factory.createMultipartConfig();
    }
}
