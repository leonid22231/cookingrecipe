package com.lyadev.cookingrecipes.repository;

import com.lyadev.cookingrecipes.entity.UserEntity;
import com.lyadev.cookingrecipes.entity.UserTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface UserTokenRepository extends CrudRepository<UserTokenEntity, Long> {
    default UserTokenEntity findByUser_id(Long id){
        for(UserTokenEntity ent: this.findAll()){
            if(ent.getUserid().getId().equals(id)){
                return ent;
            }
        }
        return null;
    }
List<UserTokenEntity> findAll();


}
