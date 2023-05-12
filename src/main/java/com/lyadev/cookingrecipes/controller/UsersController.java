package com.lyadev.cookingrecipes.controller;


import com.lyadev.cookingrecipes.entity.*;
import com.lyadev.cookingrecipes.repository.UserRepository;
import com.lyadev.cookingrecipes.service.*;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/users")
@EnableWebSecurity
@AllArgsConstructor
public class UsersController {
    private UserService userService;
    private RecipeService recipeService;
    private CommentService commentService;
    private UserTokenService userTokenService;

    PasswordEncoder passwordEncoder;

    private static Logger log = Logger.getLogger(UsersController.class.getName());

    //TODO : REGISTRATION
    @RequestMapping(value = "/registration", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody String registration(@RequestParam String login, @RequestParam String pass, @RequestParam String email){
        UserEntity user = new UserEntity();
        user.setUsername(login);
        user.setPassword(pass);
        user.setEmail(email);
       if(!userService.createUser(user)) return "Пользователь уже существует";
       userTokenService.generateTokenByUser(user);
       return user.getUsername();
    }
    @RequestMapping(value = "/registration/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody String registrationConfirm(@RequestParam String login,@RequestParam String pass, @RequestParam String token){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null)
            if(passwordEncoder.matches(pass, userEntity.getPassword())){
                if(userTokenService.getTokenById(userEntity.getId())!=null)
                    if(userTokenService.getTokenById(userEntity.getId()).equals(token)){
                        userEntity.setActivated(true);
                        userService.update(userEntity);
                        userTokenService.deleteByUser(userEntity);
                    return "Активирован";
                }
            }
        return "Ошибка";
    }
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody List<UserEntity> getAll(){
        return userService.getAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public UserEntity getUser(@PathVariable Long id){
        return userService.find(id);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody String deleteUser(@PathVariable Long id){
        if(userTokenService.getTokenByUser(userService.find(id))!=null){
            userTokenService.deleteByUser(userService.find(id));
            userService.delete(id);
        }else{
            userService.delete(id);
        }

        return "Deleted " + id;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> userLogin(@RequestParam("login") String username, @RequestParam("pass") String password){
        UserEntity userlogin = userService.findByUserName(username);
        if(userlogin!=null){
            if(passwordEncoder.matches(password,userlogin.getPassword())){
                userTokenService.generateTokenByUser(userlogin);
                return new ResponseEntity<>(new ErrorEntity(HttpStatus.OK.value(), "Sending"),HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(new ErrorEntity(HttpStatus.OK.value(), "Error"),HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/login/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody ResponseEntity<?> userLoginConfirm(@RequestParam String login,@RequestParam String pass,@RequestParam String token){
        UserEntity userEntity = userService.findByUserName(login);
        if(passwordEncoder.matches(pass, userEntity.getPassword())){
            if(userTokenService.getTokenById(userEntity.getId()).equals(token)){
                userTokenService.deleteByUser(userEntity);
                return new ResponseEntity<>(userEntity, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("Ошибка кода", HttpStatus.FAILED_DEPENDENCY);
    }
    //TODO : Forgot Pass

    @RequestMapping(value = "/login/forgotpassword/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody ResponseEntity<?> userForgotPass(@RequestParam String login, @RequestParam String email){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(userEntity.getEmail().equals(email)){
                userTokenService.generateTokenByUser(userEntity);
                return new ResponseEntity<>("Код отправлен", HttpStatus.OK);
            }
        }
        return new ResponseEntity<>("Error", HttpStatus.FAILED_DEPENDENCY);
    }

    @RequestMapping(value = "/login/forgotpassword/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody ResponseEntity<?> userForgotPassConfirm(@RequestParam String login, @RequestParam String email,@RequestParam String token, @RequestParam String new_pass){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null)
            if(userEntity.getEmail().equals(email))
                if(userTokenService.getTokenById(userEntity.getId()).equals(token))
                    if(!passwordEncoder.matches(new_pass, userEntity.getPassword())){
                        userEntity.setPassword(passwordEncoder.encode(new_pass));
                        userService.update(userEntity);
                        userTokenService.deleteByUser(userEntity);
                        return new ResponseEntity<>("Password changed", HttpStatus.OK);
                    }

        return new ResponseEntity<>("Error", HttpStatus.FAILED_DEPENDENCY);
    }
    //TODO : INFO
    @RequestMapping(value = "/{login}/Info/Id", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Long getIt(@PathVariable String login) {
        return userService.findByUserName(login).getId();
    }

    @RequestMapping(value = "/{login}/Info/Name", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity getName(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return ResponseEntity.ok(userEntity.getName());
        }
    return ResponseEntity.badRequest().body("User not found");
    }

    @RequestMapping(value = "/{login}/Info/Name", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public UserEntity setName(@PathVariable String login,@RequestParam String pass,@RequestParam String name){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.setName(name);
                userService.update(userEntity);
                return userEntity;
            }
        }
        return null;
    }

    @RequestMapping(value = "/{login}/Info/Location", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity getLocation(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return ResponseEntity.ok(userEntity.getLocation());
        }
        return ResponseEntity.badRequest().body("User not found");
    }

    @RequestMapping(value = "/{login}/Info/Location", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public UserEntity setLocation(@PathVariable String login,@RequestParam String pass,@RequestParam String location){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.setLocation(location);
                userService.update(userEntity);
                return userEntity;
            }
        }
        return null;
    }

    @RequestMapping(value = "/{login}/Info/Email", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity getEmail(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return ResponseEntity.ok(userEntity.getEmail());
        }
        return ResponseEntity.badRequest().body("User not found");
    }
    @RequestMapping(value = "/{login}/Info/Image", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public UserEntity setImage(@PathVariable String login,@RequestParam String pass,@RequestParam String image){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.setImage(image);
                userService.update(userEntity);
                return userEntity;
            }
        }
        return null;
    }
    @RequestMapping(value = "/{login}/Info/Image", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity getImage(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return ResponseEntity.ok(userEntity.getImage());
        }
        return ResponseEntity.badRequest().body("User not found");
    }
    @RequestMapping(value = "/{login}/Info/Secondname", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public UserEntity setSecondName(@PathVariable String login,@RequestParam String pass,@RequestParam String secondname){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.setSecondname(secondname);
                userService.update(userEntity);
                return userEntity;
            }
        }
        return null;
    }
    @RequestMapping(value = "/get/{id}/Info/Name", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public String getNameById(@PathVariable Long id){
        return userService.find(id).getUsername();
    }
    //TODO: FAVORITE
    @RequestMapping(value = "/{login}/Favorite/Add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<RecipeEntity> AddFavorite( @PathVariable String login , @RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                RecipeEntity recipeEntity = recipeService.find(id);
                userEntity.addFavorite(recipeEntity);
                userService.update(userEntity);
            }
            }
        return userEntity.getFavotite();
    }
    @RequestMapping(value = "/{login}/Favorite", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<RecipeEntity> GetFavorite(@PathVariable String login){

        return userService.findByUserName(login).getFavotite();
    }

    @RequestMapping(value = "/{login}/Favorite/Delete",method = RequestMethod.DELETE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<RecipeEntity> DeleteFavorite(@PathVariable String login, @RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
               userEntity.deleteFavorite(id);
               userService.update(userEntity);
            }
        }
        return userEntity.getFavotite();
    }

    @RequestMapping(value = "/{login}/Favorite/{id}",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public RecipeEntity GetFavorite(@PathVariable String login,@PathVariable Long id, @RequestParam String pass){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                for(RecipeEntity  ent : userEntity.getFavotite()){
                    if(ent.getId().equals(id)){
                        return ent;
                    }
                }

            }
        }
        return null;
    }
    //TODO: RECIPES
    @RequestMapping(value = "/{login}/Recipes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<RecipeEntity> GetRecipes(@PathVariable String login){

        return userService.findByUserName(login).getRecipes();
    }
    @RequestMapping(value = "/{login}/Recipes/Add",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<RecipeEntity> AddRecipes(@PathVariable String login,@RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.getRecipes().add(recipeService.find(id));
                userService.update(userEntity);
            }
        }
        return userEntity.getRecipes();
    }
    @RequestMapping(value = "/{login}/Recipes/Delete",method = RequestMethod.DELETE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<RecipeEntity> DeleteRecipes(@PathVariable String login,@RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.deleteRecipe(id);
                userService.update(userEntity);
            }
        }
        return userEntity.getRecipes();
    }
    @RequestMapping(value = "/{login}/Recipes/{id}",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public RecipeEntity GetRecipes(@PathVariable String login,@PathVariable Long id, @RequestParam String pass){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                for(RecipeEntity  ent : userEntity.getRecipes()){
                    if(ent.getId().equals(id)){
                        return ent;
                    }
                }

            }
        }
        return null;
    }
    @RequestMapping(value = "/{login}/Recipes/{id}/Name", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetRecipeName(@PathVariable String login, @PathVariable Long id, @RequestParam String pass){

        return new ResponseEntity<>(HttpStatus.OK);
    }
    //TODO: COMMENTS
    @RequestMapping(value = "/{login}/Comments",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<CommentEntity> GetComments(@PathVariable String login, @RequestParam String pass) {
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                return userEntity.getComments();
            }
            }
    return null;
    }

    @RequestMapping(value = "/{login}/Comments/Add",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<CommentEntity> AddComment(@PathVariable String login, @RequestParam String pass,@RequestBody CommentEntity comment) {
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                comment.setUser(userEntity);
                comment.setUser_name(userEntity.getUsername());
                comment.setRecipe(recipeService.find(comment.getRecipeId()));
                comment.setCreatedate(Calendar.getInstance().getTime());
                commentService.createComment(comment);
                userEntity.getComments().add(comment);
                userService.update(userEntity);
                recipeService.updateRate(commentService.find(comment.getId()).getRecipe().getId());
            }
        }
        return userEntity.getComments();
    }

    @RequestMapping(value = "/{login}/Comments/Delete",method = RequestMethod.DELETE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<CommentEntity> DeleteComment(@PathVariable String login, @RequestParam String pass,@RequestParam Long id) {
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                    for (CommentEntity comm : userEntity.getComments()) {
                        log.info(comm.getId().toString());
                        if (comm.getId().equals(id)) {
                            boolean comment = userEntity.getComments().remove(comm);
                            break;
                        }
                    }
                userService.update(userEntity);
                commentService.remove(id);
            }

        }
        return userEntity.getComments();
    }

    @RequestMapping(value = "/{login}/Comments/{id}",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public CommentEntity GetComment(@PathVariable String login,@PathVariable Long id, @RequestParam String pass) {
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                for(CommentEntity ent : userEntity.getComments()){
                    if(ent.getId().equals(id)){
                        return ent;
                    }
                }
            }
        }
        return null;
    }
    //TODO : COOKED
    @RequestMapping(value = "/{login}/Cooked", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetCooked(@PathVariable String login, @RequestParam String pass){
        UserEntity userEntity = userService.findByUserName(login);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(userEntity!=null) {
            if(userEntity.getUsername().equals(auth.getName())){
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {
                return new ResponseEntity<>(userEntity.getCooked(), HttpStatus.OK);
            }
            }else{return new ResponseEntity<>("Доступ запрещен", HttpStatus.UNAUTHORIZED);}
        }
        return new ResponseEntity<>("Ошибка",HttpStatus.FAILED_DEPENDENCY);
    }

    @RequestMapping(value = "/{login}/Cooked/Add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<RecipeEntity> AddCooked(@PathVariable String login,@RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {
                userEntity.getCooked().add(recipeService.find(id));
                userService.update(userEntity);
                return userEntity.getCooked();
            }
        }
        return null;
    }

    @RequestMapping(value = "/{login}/Cooked/Delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public Set<RecipeEntity> DeleteCooked(@PathVariable String login, @RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {
                if(userEntity.getCooked().size()>0)
                    for(RecipeEntity ent : userEntity.getCooked()){
                        log.info(ent.getId().toString());
                        if(ent.getId().equals(id)) {
                            userEntity.getCooked().remove(ent);
                            userService.update(userEntity);
                            break;
                        }
                }
            }
        }
        return userEntity.getCooked();
    }

    @RequestMapping(value = "/{login}/Cooked/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public RecipeEntity getCooked(@PathVariable String login, @PathVariable Long id, @RequestParam String pass){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {
            }
            if(userEntity.getCooked().size()>0)
                for(RecipeEntity ent : userEntity.getCooked()){
                    if(ent.getId().equals(id)){
                        return recipeService.find(id);
                    }
                }
            }
        return null;
    }

    }

