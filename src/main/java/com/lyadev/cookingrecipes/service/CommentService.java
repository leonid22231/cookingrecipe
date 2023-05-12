package com.lyadev.cookingrecipes.service;

import com.lyadev.cookingrecipes.entity.CommentEntity;
import com.lyadev.cookingrecipes.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    public CommentEntity find(Long id){return commentRepository.findById(id).get();}
    public List<CommentEntity> getAll(){return commentRepository.findAll();}

    public void createComment(CommentEntity commentEntity){commentRepository.save(commentEntity);}
    public void deleteComment(CommentEntity commentEntity){commentRepository.delete(commentEntity);}
    public void remove(Long id){commentRepository.deleteById(id);}
}
