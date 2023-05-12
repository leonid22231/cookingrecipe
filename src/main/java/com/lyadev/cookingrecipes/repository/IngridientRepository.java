package com.lyadev.cookingrecipes.repository;

import com.lyadev.cookingrecipes.entity.IngridientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

public interface IngridientRepository extends CrudRepository<IngridientEntity, Long> {
    public Set<IngridientEntity> findAll();
}
