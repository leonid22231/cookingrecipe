package com.lyadev.cookingrecipes.repository;

import com.lyadev.cookingrecipes.entity.RecipeEntity;
import com.lyadev.cookingrecipes.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    UserEntity findByUsername(String username);
    List<RecipeEntity> getByIdOrderByFavotite(Long id);
    /*@Modifying
    @Query("delete from user_favotite where user_entity_id = :user and favotite_id = :id;")
    static void deleteFavoriteById(@Param("user")Long user,@Param("id") Long id);*/
}
