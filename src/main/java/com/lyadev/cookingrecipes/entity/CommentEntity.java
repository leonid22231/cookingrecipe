package com.lyadev.cookingrecipes.entity;

import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Table(name = "comment")
@Getter
@Setter
@Entity
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "text")
    private String text;

    @Column(name = "rating")
    private double rating;

    @Column(name = "createdate")
    private Date createdate;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user")
    UserEntity user;

    @Column(name = "user", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "user_name")
    private String user_name ;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "recipe", referencedColumnName = "id")
    private RecipeEntity recipe;

    @Column(name = "recipe", insertable = false, updatable = false)
    private Long recipeId;
}
