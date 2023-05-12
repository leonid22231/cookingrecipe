package com.lyadev.cookingrecipes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ingridient")
@Getter
@Setter

public class IngridientEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", unique = true, length = 30)
    private String name;

    @Override
    public String toString() {
        return "IngridientEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
