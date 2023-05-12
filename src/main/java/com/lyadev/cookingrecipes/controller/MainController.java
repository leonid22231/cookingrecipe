package com.lyadev.cookingrecipes.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@EnableWebSecurity
public class MainController {
    @GetMapping("")
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody String test(){
        return "<form action='/api/recipes'>"+
                "<button >api/recipes</button>" +
                "</form>" +
                "<form action='/api/recipes/add'>" +
                "<button>api/recipes/add</button>" +
                "</form>";
    }
}
