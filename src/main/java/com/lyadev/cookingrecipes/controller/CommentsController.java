package com.lyadev.cookingrecipes.controller;

import com.lyadev.cookingrecipes.entity.CommentEntity;
import com.lyadev.cookingrecipes.entity.UserEntity;
import com.lyadev.cookingrecipes.service.CommentService;
import com.lyadev.cookingrecipes.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/comments")
@EnableWebSecurity
public class CommentsController {
    @Autowired
    private CommentService commentService;
    @Autowired
    private UserService userService;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public List<CommentEntity> getAll(){
        return commentService.getAll();
    }

    @RequestMapping(value = "/Add", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody  List<CommentEntity> addComment(@RequestBody CommentEntity commentEntity){
       commentService.createComment(commentEntity);
       /*UserEntity userEntity = userService.find(commentEntity.getUser().getId());
       userEntity.getComments().add(commentEntity);*/

        //userService.update(userEntity);
        return commentService.getAll();
    }
    @RequestMapping(value = "/Delete", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.OK)
    public List<CommentEntity> deleteComment(@RequestParam Long id){
        commentService.deleteComment(commentService.find(id));
        return commentService.getAll();
    }
}
