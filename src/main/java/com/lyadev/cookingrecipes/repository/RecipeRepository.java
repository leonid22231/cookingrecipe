package com.lyadev.cookingrecipes.repository;

import com.lyadev.cookingrecipes.entity.RecipeEntity;
import org.springframework.data.repository.CrudRepository;


public interface RecipeRepository extends CrudRepository<RecipeEntity, Long> {
}
