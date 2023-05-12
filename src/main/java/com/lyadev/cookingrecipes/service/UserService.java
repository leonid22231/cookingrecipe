package com.lyadev.cookingrecipes.service;

import com.lyadev.cookingrecipes.entity.RecipeEntity;
import com.lyadev.cookingrecipes.entity.UserEntity;
import com.lyadev.cookingrecipes.entity.enums.Role;
import com.lyadev.cookingrecipes.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean createUser(UserEntity userEntity){
        if(userRepository.findByUsername(userEntity.getUsername())!=null) return false;
        userEntity.getRoles().add(Role.USER);
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userRepository.save(userEntity);
        return true;
    }
    public void update(UserEntity userEntity){userRepository.save(userEntity);}
    public List<RecipeEntity> test(Long id){
        return userRepository.getByIdOrderByFavotite(id);
    }

    public UserEntity findByUserName(String username){return userRepository.findByUsername(username);}
    public UserEntity find(Long id){return userRepository.findById(id).get();}
    public List<UserEntity> getAll(){
        return (List<UserEntity>) userRepository.findAll();
    }
    public List<RecipeEntity> getFavorite(){
        return null;
    }
    public void delete(Long id){userRepository.deleteById(id);}

}
