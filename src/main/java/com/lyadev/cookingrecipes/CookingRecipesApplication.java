package com.lyadev.cookingrecipes;

import com.lyadev.cookingrecipes.entity.UserEntity;
import com.lyadev.cookingrecipes.service.SimpleEmailService;
import com.lyadev.cookingrecipes.service.UserTokenService;
import com.lyadev.cookingrecipes.storage.StorageProperties;
import com.lyadev.cookingrecipes.storage.StorageService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.mail.SimpleMailMessage;

import java.util.Set;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class CookingRecipesApplication {

    public static void main(String[] args) {
        SpringApplication.run(CookingRecipesApplication.class, args);

    }
    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {

            storageService.init();

        };}

}
