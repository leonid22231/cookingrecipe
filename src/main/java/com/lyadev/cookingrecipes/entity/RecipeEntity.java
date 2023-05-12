package com.lyadev.cookingrecipes.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Set;


@Table(name = "recipe")
@Getter
@Setter
@Entity
public class RecipeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "image")
    private String image;

    @Column(name = "createdate")
    @Temporal(TemporalType.DATE)
    private Date createdate;

    @Column(name = "recipe")
    private String recipe;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    @JsonBackReference(value = "comm_recipe")
    private Set<CommentEntity> comments;

    @Column(name = "ingtidients")
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<IngridientEntity> ingtidients;

    @JsonIgnore
    @ManyToOne(targetEntity = UserEntity.class)
    private UserEntity author ;

    @Column(name = "author_id", insertable = false, updatable = false)
    private Long author_id ;

    @Column(name = "rate")
    private Double rate;
}
