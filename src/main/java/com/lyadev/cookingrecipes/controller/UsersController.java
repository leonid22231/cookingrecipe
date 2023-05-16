package com.lyadev.cookingrecipes.controller;


import com.lyadev.cookingrecipes.entity.*;
import com.lyadev.cookingrecipes.entity.enums.MessageCodes;
import com.lyadev.cookingrecipes.service.*;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Calendar;
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

    private static final Logger log = Logger.getLogger(UsersController.class.getName());

    @RequestMapping(value = "/registration", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> registration(@RequestParam String login, @RequestParam String pass, @RequestParam String email){
        UserEntity user = new UserEntity();
        user.setUsername(login);
        user.setPassword(pass);
        user.setEmail(email);
       if(!userService.createUser(user)) return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_EXISTS,"Пользователь с логином " + login + " уже существует"), HttpStatus.OK);
       userTokenService.generateTokenByUser(user);
       return new ResponseEntity<>(new MessageEntity(MessageCodes.CODE_SENDED, "Код отправлен на адрес " + email),HttpStatus.OK);
    }
    @RequestMapping(value = "/registration/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> registrationConfirm(@RequestParam String login,@RequestParam String pass, @RequestParam String token){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {
                if (userTokenService.getTokenById(userEntity.getId()) != null) {
                    if (userTokenService.getTokenById(userEntity.getId()).equals(token)) {
                        userEntity.setActivated(true);
                        userService.update(userEntity);
                        userTokenService.deleteByUser(userEntity);
                        return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_CREATED, "Пользователь " + userEntity.getUsername() + " успешно создан"), HttpStatus.OK);
                    } else return new ResponseEntity<>(new MessageEntity(MessageCodes.CODE_WRONG, "Неверный код для пользователя " + userEntity.getUsername()), HttpStatus.OK);
                } else return new ResponseEntity<>(new MessageEntity(MessageCodes.CODE_NOT_FOUND, "Код для пользователя " + userEntity.getUsername() + " не найдет"), HttpStatus.OK);
            } else return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + userEntity.getUsername()), HttpStatus.OK);
        }else return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "", method = RequestMethod.GET)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getAll(){
        return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userService.getAll()), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getUser(@PathVariable Long id){
        UserEntity userEntity = userService.find(id);
        if(userEntity!=null){
            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ id + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        UserEntity userEntity = userService.find(id);
        if(userEntity!=null){
            if (userTokenService.getTokenByUser(userService.find(id)) != null){
                userTokenService.deleteByUser(userService.find(id));
            }
            userService.delete(id);
            return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_DELETED,"Пользователь " + id +"удален"), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + id + " не найден"),HttpStatus.OK);
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> userLogin(@RequestParam("login") String login, @RequestParam("pass") String password){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(password,userEntity.getPassword())){
                userTokenService.generateTokenByUser(userEntity);
                return new ResponseEntity<>(new MessageEntity(MessageCodes.CODE_SENDED, "Код отправлен на адрес " + userEntity.getEmail()),HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"),HttpStatus.OK);
    }

    @RequestMapping(value = "/login/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> userLoginConfirm(@RequestParam String login,@RequestParam String pass,@RequestParam String token){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if(passwordEncoder.matches(pass, userEntity.getPassword())) {
                if(userTokenService.getTokenById(userEntity.getId()).equals(token)) {
                    userTokenService.deleteByUser(userEntity);
                    return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity), HttpStatus.OK);
                }return new ResponseEntity<>(new MessageEntity(MessageCodes.CODE_WRONG, "Неверный код для пользователя " + userEntity.getUsername()), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/login/forgot-password/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody ResponseEntity<?> userForgotPass(@RequestParam String login, @RequestParam String email){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(userEntity.getEmail().equals(email)){
                userTokenService.generateTokenByUser(userEntity);
                return new ResponseEntity<>("Код отправлен", HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.EMAIL_WRONG, "Неверный email для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/login/forgot-password/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> userForgotPassConfirm(@RequestParam String login, @RequestParam String email,@RequestParam String token, @RequestParam String new_pass){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if (userEntity.getEmail().equals(email)) {
                if (userTokenService.getTokenById(userEntity.getId()).equals(token)) {
                    if (!passwordEncoder.matches(new_pass, userEntity.getPassword())) {
                        userEntity.setPassword(passwordEncoder.encode(new_pass));
                        userService.update(userEntity);
                        userTokenService.deleteByUser(userEntity);
                        return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_CHANGED,"Пароль для пользователя " + login +" успешно изменен"), HttpStatus.OK);
                    }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
                }return new ResponseEntity<>(new MessageEntity(MessageCodes.CODE_WRONG, "Неверный код для пользователя " + userEntity.getUsername()), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.EMAIL_WRONG, "Неверный email для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Info/Id", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getIt(@PathVariable String login) {
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getId()), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Info/Name", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getName(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getName()), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Info/Name", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> setName(@PathVariable String login,@RequestParam String pass,@RequestParam String name){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.setName(name);
                userService.update(userEntity);
                return new ResponseEntity<>(new MessageEntity(MessageCodes.INFO_CHANGED, "Имя: '"+name+"' для пользователя " + userEntity.getName()+" успешно сохранено"),HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Info/Location", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getLocation(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getLocation()),HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Info/Location", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> setLocation(@PathVariable String login,@RequestParam String pass,@RequestParam String location){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.setLocation(location);
                userService.update(userEntity);
                return new ResponseEntity<>(new MessageEntity(MessageCodes.INFO_CHANGED, "Местоположение: '"+location+"' для пользователя " + userEntity.getName()+" успешно сохранено"),HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Info/Email", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getEmail(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getEmail()), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Info/Image", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> setImage(@PathVariable String login,@RequestParam String pass,@RequestParam String image){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.setImage(image);
                userService.update(userEntity);
                return new ResponseEntity<>(new MessageEntity(MessageCodes.INFO_CHANGED, "Изображение: '"+image+"' для пользователя " + userEntity.getName()+" успешно сохранено"),HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь "+ login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Info/Image", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getImage(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getImage()), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);

    }
    @RequestMapping(value = "/{login}/Info/Secondname", method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> setSecondName(@PathVariable String login,@RequestParam String pass,@RequestParam String secondname){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.setSecondname(secondname);
                userService.update(userEntity);
                return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getSecondname()), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/get/{id}/Info/Name", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getNameById(@PathVariable Long id){
        UserEntity userEntity = userService.find(id);
        if(userEntity!=null){
            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getName()), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + id + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Favorite/Add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> AddFavorite( @PathVariable String login , @RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                RecipeEntity recipeEntity = recipeService.find(id);
                userEntity.addFavorite(recipeEntity);
                userService.update(userEntity);
                return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, "Избранное " + recipeEntity.getId() + " для пользователя "+login+" добавлено"),HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Favorite", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetFavorite(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getFavotite()), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Favorite/Delete",method = RequestMethod.DELETE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> DeleteFavorite(@PathVariable String login, @RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
               userEntity.deleteFavorite(id);
               userService.update(userEntity);
               return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, "Избранное " + id + " для пользователя " + login + " успешно удаленно"),HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Favorite/{id}",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetFavorite(@PathVariable String login,@PathVariable Long id, @RequestParam String pass){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                for(RecipeEntity  ent : userEntity.getFavotite()){
                    if(ent.getId().equals(id)){
                        return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, ent), HttpStatus.OK);
                    }
                }return new ResponseEntity<>(new MessageEntity(MessageCodes.RECIPE_NOT_FOUND, "Рецепт " + id + " для пользователя "+ login + " не найден"), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Recipes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetRecipes(@PathVariable String login){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getRecipes()), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Recipes/Add",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> AddRecipes(@PathVariable String login,@RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.getRecipes().add(recipeService.find(id));
                userService.update(userEntity);
                return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, "Рецепт " + id + " для пользователя "+login+ " добавлен "),HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Recipes/Delete",method = RequestMethod.DELETE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> DeleteRecipes(@PathVariable String login,@RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                userEntity.deleteRecipe(id);
                userService.update(userEntity);
                return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, "Рецепт " + id + " пользователя "+ login + " удален"), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Recipes/{id}",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetRecipes(@PathVariable String login,@PathVariable Long id, @RequestParam String pass){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                for(RecipeEntity  ent : userEntity.getRecipes()){
                    if(ent.getId().equals(id)){
                        return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, ent), HttpStatus.OK);
                    }
                }return new ResponseEntity<>(new MessageEntity(MessageCodes.RECIPE_NOT_FOUND, "Рецепт " + id + " для пользователя "+ login + "не найден"), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Recipes/{id}/Name", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetRecipeName(@PathVariable String login, @PathVariable Long id, @RequestParam String pass){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {
                for(RecipeEntity ent : userEntity.getRecipes()){
                    if(ent.getId().equals(id)){
                        return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, ent.getName()), HttpStatus.OK);
                    }
                }return new ResponseEntity<>(new MessageEntity(MessageCodes.RECIPE_NOT_FOUND, "Рецепт " + id + " для пользователя "+ login + "не найден"), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Comments",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetComments(@PathVariable String login, @RequestParam String pass) {
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, userEntity.getComments()), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Comments/Add",method = RequestMethod.POST,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> AddComment(@PathVariable String login, @RequestParam String pass,@RequestBody CommentEntity comment) {
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
                return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, "Комментарий к рецепту " + comment.getRecipe().getId() + " от пользователя " + login+ " успешно добавлен"), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Comments/Delete",method = RequestMethod.DELETE,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> DeleteComment(@PathVariable String login, @RequestParam String pass,@RequestParam Long id) {
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                    for (CommentEntity comm : userEntity.getComments()) {
                        log.info(comm.getId().toString());
                        if (comm.getId().equals(id)) {
                            userEntity.getComments().remove(comm);
                            userService.update(userEntity);
                            commentService.remove(id);
                            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, "Комментарий " + id + " пользователя " + login + " рецепта " + comm.getRecipe().getId()+" успешно удален"), HttpStatus.OK);
                        }
                    }return new ResponseEntity<>(new MessageEntity(MessageCodes.COMMENT_NOT_FOUND, "Комментарий "+ id + " пользователя " + login + " не найден"), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Comments/{id}",method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetComment(@PathVariable String login,@PathVariable Long id, @RequestParam String pass) {
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null){
            if(passwordEncoder.matches(pass,userEntity.getPassword())) {
                for(CommentEntity ent : userEntity.getComments()){
                    if(ent.getId().equals(id)){
                        return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, ent), HttpStatus.OK);
                    }
                }return new ResponseEntity<>(new MessageEntity(MessageCodes.COMMENT_NOT_FOUND, "Комментарий "+ id + " пользователя "+ login + " не найден"), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
    @RequestMapping(value = "/{login}/Cooked", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> GetCooked(@PathVariable String login, @RequestParam String pass){
        UserEntity userEntity = userService.findByUserName(login);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(userEntity!=null) {
            if(userEntity.getUsername().equals(auth.getName())){
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {
                return new ResponseEntity<>(userEntity.getCooked(), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
            }else{return new ResponseEntity<>("Доступ запрещен", HttpStatus.UNAUTHORIZED);}
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Cooked/Add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> AddCooked(@PathVariable String login,@RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {
                userEntity.getCooked().add(recipeService.find(id));
                userService.update(userEntity);
                return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, "Рецепт " + id + " для пользователя "+ login + " успешно добавлен"), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Cooked/Delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> DeleteCooked(@PathVariable String login, @RequestParam String pass, @RequestParam Long id){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {
                if(userEntity.getCooked().size()>0)
                    for(RecipeEntity ent : userEntity.getCooked()){
                        log.info(ent.getId().toString());
                        if(ent.getId().equals(id)) {
                            userEntity.getCooked().remove(ent);
                            userService.update(userEntity);
                            return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, "Рецепт " +  id + " пользователя " + login + " успешно удален"), HttpStatus.OK);
                        }
                }
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
        }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }

    @RequestMapping(value = "/{login}/Cooked/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<?> getCooked(@PathVariable String login, @PathVariable Long id, @RequestParam String pass){
        UserEntity userEntity = userService.findByUserName(login);
        if(userEntity!=null) {
            if (passwordEncoder.matches(pass, userEntity.getPassword())) {

            if(userEntity.getCooked().size()>0)
                for(RecipeEntity ent : userEntity.getCooked()){
                    if(ent.getId().equals(id)){
                        return new ResponseEntity<>(new MessageEntity(MessageCodes.OK, ent), HttpStatus.OK);
                    }
                }return new ResponseEntity<>(new MessageEntity(MessageCodes.RECIPE_NOT_FOUND, "Рецепт " + id + " пользователя "+ login + " не найден"), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.PASSWORD_WRONG, "Неверный пароль для пользователя " + login), HttpStatus.OK);
            }return new ResponseEntity<>(new MessageEntity(MessageCodes.USER_NOT_FOUND, "Пользователь " + login + " не найден"), HttpStatus.OK);
    }
}

