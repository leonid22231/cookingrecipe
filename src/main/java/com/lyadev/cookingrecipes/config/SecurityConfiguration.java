package com.lyadev.cookingrecipes.config;

import com.lyadev.cookingrecipes.entity.enums.Role;
import com.lyadev.cookingrecipes.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration{
    @Autowired
    private UsersService usersService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf().disable()
                .authorizeRequests().requestMatchers("api/v1/recipes", "api/v1/users/registration/**","api/v1/users/login", "api/v1/recipes/{id}", "api/v1/recipes/{id}/Comments", "api/v1/users/get/{id}/Info/Name", "api/v1/files/get/{filename:.+}","api/v1/recipes/{id}/Comments", "api/v1/users/login/confirm", "api/v1/users/login/forgotpassword/**").permitAll()
                .and().authorizeRequests().requestMatchers("api/v1/recipes/add", "api/v1/users/{login}/**", "api/v1/ingredients/**", "api/v1/files/Add", "api/v1/users/leonid/Favorite/**").hasAuthority("USER").and()
                .authorizeRequests().requestMatchers(HttpMethod.POST, "api/v1/recipes/add").hasAuthority("USER").and()
                .authorizeRequests().requestMatchers("api/**").hasRole("ADMIN").anyRequest().authenticated()
                .and()
                .httpBasic();

        return httpSecurity.build();
    }

    @Autowired
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
    authenticationManagerBuilder.userDetailsService(usersService).passwordEncoder(passwordEncoder);
    }

}
