package com.lyadev.cookingrecipes.service;

import com.lyadev.cookingrecipes.entity.CommentEntity;
import com.lyadev.cookingrecipes.entity.IngridientEntity;
import com.lyadev.cookingrecipes.entity.RecipeEntity;
import com.lyadev.cookingrecipes.repository.RecipeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RecipeService {
    private RecipeRepository recipeRepository;

    public RecipeEntity create(RecipeEntity recipeEntity){
        return recipeRepository.save(recipeEntity);
    }
    public RecipeEntity find(Long id){
        return recipeRepository.findById(id).get();
    }
    public void delete(Long id){
        recipeRepository.deleteById(id);
    }
    public List<RecipeEntity> getAll(){
       return (List<RecipeEntity>) recipeRepository.findAll();
    }
    public void updateRate(Long id){
        RecipeEntity recipe = recipeRepository.findById(id).get();
        double rate = 0;
        int i = 0;
        for(CommentEntity comment : recipe.getComments()){
            if(comment.getRating()!=0){
                rate += comment.getRating();
                i++;
            }

        }
        recipe.setRate((double) rate/i);
        recipeRepository.save(recipe);
    }
    public void update(RecipeEntity recipe){
        for(IngridientEntity ingridientEntity : recipe.getIngtidients()){
            System.out.println(ingridientEntity.toString());
        }
        recipeRepository.save(recipe);
    }


}
