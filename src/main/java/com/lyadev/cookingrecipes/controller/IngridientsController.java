package com.lyadev.cookingrecipes.controller;

import com.lyadev.cookingrecipes.entity.CommentEntity;
import com.lyadev.cookingrecipes.entity.IngridientEntity;
import com.lyadev.cookingrecipes.service.IngridientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/ingredients")
@EnableWebSecurity
public class IngridientsController {
    @Autowired
    private IngridientService ingridientService;

    @RequestMapping(value = "/Add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public String addComment(@RequestParam String name){
        IngridientEntity ingridientEntity = new IngridientEntity();
        ingridientEntity.setName(name);
        ingridientService.add(ingridientEntity);
        return name;
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<IngridientEntity> addComment(){

        return ingridientService.getAll();
    }

}
