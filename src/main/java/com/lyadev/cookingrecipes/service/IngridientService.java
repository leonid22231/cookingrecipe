package com.lyadev.cookingrecipes.service;

import com.lyadev.cookingrecipes.entity.IngridientEntity;
import com.lyadev.cookingrecipes.repository.IngridientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class IngridientService {
    IngridientRepository ingridientRepository;
    public void add(IngridientEntity name){
        ingridientRepository.save(name);
    }
    public Set<IngridientEntity> getAll(){
        return ingridientRepository.findAll();
    }
}
