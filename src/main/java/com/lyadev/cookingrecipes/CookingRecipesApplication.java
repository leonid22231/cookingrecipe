package com.lyadev.cookingrecipes;


import com.lyadev.cookingrecipes.storage.StorageProperties;
import com.lyadev.cookingrecipes.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;



@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class CookingRecipesApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CookingRecipesApplication.class, args);

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(CookingRecipesApplication.class);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {

            storageService.init();

        };}

}
