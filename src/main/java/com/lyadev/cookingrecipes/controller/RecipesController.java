package com.lyadev.cookingrecipes.controller;

import com.lyadev.cookingrecipes.entity.CommentEntity;
import com.lyadev.cookingrecipes.entity.IngridientEntity;
import com.lyadev.cookingrecipes.entity.RecipeEntity;
import com.lyadev.cookingrecipes.entity.UserEntity;
import com.lyadev.cookingrecipes.service.IngridientService;
import com.lyadev.cookingrecipes.service.RecipeService;
import com.lyadev.cookingrecipes.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
import java.util.List;
import java.util.Set;


@RestController
@RequestMapping("/api/v1/recipes")
@AllArgsConstructor
@EnableWebSecurity
public class RecipesController {
    private RecipeService recipeService;
    private UserService userService;
    private IngridientService ingridientService;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody List<RecipeEntity> getAll(){
        return recipeService.getAll();
    }

    @RequestMapping(value = "/delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public String  deleteRecipe(@RequestParam Long id){
        RecipeEntity recipeEntity = recipeService.find(id);
        for(UserEntity user : userService.getAll()){
            if(user.getFavotite()!=null && user.getCooked()!=null) {
                for (RecipeEntity recipe : user.getFavotite()) {
                    if (recipeEntity.getId().equals(recipe.getId())) {
                        user.getFavotite().remove(recipe);
                        break;
                    }
                }
                for (RecipeEntity recipe : user.getCooked()) {
                    if (recipeEntity.getId().equals(recipe.getId())) {
                        user.getCooked().remove(recipe);
                        break;
                    }
                }
                userService.update(user);
            }

        }
        recipeService.delete(id);
        return "ok";
    }
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public RecipeEntity getRecipe(@PathVariable Long id){
    return recipeService.find(id);
    }

    @RequestMapping(value = "/{id}/Comments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<CommentEntity> getRecipeComments(@PathVariable Long id){
        return recipeService.find(id).getComments();
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> createrecipe(@AuthenticationPrincipal UserEntity user, @RequestParam String name, @RequestParam String image, @RequestParam String recipe, @RequestBody Set<IngridientEntity> ingridient){
        RecipeEntity recipeEntity = new RecipeEntity();
        recipeEntity.setName(name);
        recipeEntity.setImage(image);
        recipeEntity.setRecipe(recipe);
        for(IngridientEntity ingridientEntity : ingridient){
            Boolean check = false;
            for(IngridientEntity ingridientEntity1 : ingridientService.getAll()){
                if(ingridientEntity.getName().equals(ingridientEntity1.getName())){
                    check = true;
                    ingridientEntity.setId(ingridientEntity1.getId());
                    break;
                }
            }
            if(!check){
                IngridientEntity ingridientEntity1 = new IngridientEntity();
                ingridientEntity1.setName(ingridientEntity.getName());
                ingridientService.add(ingridientEntity1);
                ingridientEntity.setId(ingridientEntity1.getId());
            }
        }

        recipeEntity.setIngtidients(ingridient);
        recipeEntity.setCreatedate(Calendar.getInstance().getTime());
        recipeEntity.setAuthor(user);
        recipeEntity.setRate((double)0.0);
        recipeService.create(recipeEntity);
        user.getRecipes().add(recipeService.find(recipeEntity.getId()));
        userService.update(user);
        return new ResponseEntity<>(recipeEntity.getId(),HttpStatus.OK);
    }
    @RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public String updateRecipe(@RequestBody RecipeEntity recipe){
        for(IngridientEntity ingridientEntity : recipe.getIngtidients()){
            Boolean check = false;
            for(IngridientEntity ingridientEntity1 : ingridientService.getAll()){
                if(ingridientEntity.getName().equals(ingridientEntity1.getName())){
                    check = true;
                    ingridientEntity.setId(ingridientEntity1.getId());
                    break;
                }
            }
            if(!check){
                IngridientEntity ingridientEntity1 = new IngridientEntity();
                ingridientEntity1.setName(ingridientEntity.getName());
                ingridientService.add(ingridientEntity1);
                ingridientEntity.setId(ingridientEntity1.getId());
            }
        }
        recipe.setAuthor(userService.find(recipe.getAuthor_id()));
        recipe.setCreatedate(recipeService.find(recipe.getId()).getCreatedate());
        recipe.setComments(recipeService.find(recipe.getId()).getComments());
        if(recipeService.find(recipe.getId()).getComments().size()>0){
            double rate = 0;
            int i = 0;
            for(CommentEntity commentEntity : recipeService.find(recipe.getId()).getComments()){
                rate+= commentEntity.getRating();
                i++;
            }
            recipe.setRate(rate/i);
        }else {
            recipe.setRate((double)0.0);
        }
        recipeService.update(recipe);
        recipeService.updateRate(recipe.getId());
        return "yes";
    }
    @RequestMapping(value = "/up", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public List<RecipeEntity> update(){
        for(RecipeEntity recipe : recipeService.getAll()){
            double rate = 0;
            int i = 0;
            if(recipe.getComments().size()>0) {
                for (CommentEntity commentEntity : recipe.getComments()) {
                    if(rate != 0){
                        i++;
                        rate += commentEntity.getRating();
                    }

                }
                recipe.setRate((double) (rate / i));

            }else {
                recipe.setRate((double) 0);

            }
            recipeService.create(recipe);
        }
        return recipeService.getAll();
    }

}
