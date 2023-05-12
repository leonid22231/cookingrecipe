package com.lyadev.cookingrecipes.entity;

import com.fasterxml.jackson.annotation.*;
import com.lyadev.cookingrecipes.entity.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Entity
@Table(name = "user")
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id"
)
public class UserEntity implements UserDetails {
    public UserEntity(){
        this.activated = false;
        this.roles .add(Role.USER);
        this.date = Calendar.getInstance().getTime();
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username")
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "name")
    private String name;

    @Column(name = "secondname")
    private String secondname;


    @Column(name = "email", unique = true, length = 50)
    private String email;

    @Column(name = "location")
    private String location;

    @Column(name = "image")
    private String image;

    @JsonIgnore
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<RecipeEntity> recipes;

    @JsonIgnore
    @Column(name = "favotite")
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<RecipeEntity> favotite ;

    @Column(name = "cooked")
    @ManyToMany()
    private Set<RecipeEntity> cooked;

    @Column(name = "comments")
    @OneToMany(mappedBy = "user",fetch = FetchType.LAZY)
    private Set<CommentEntity> comments;

    @Column(name = "createdate")
    private Date date;

    @Column(name = "activated")
    private Boolean activated;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role", joinColumns = @JoinColumn(name = "id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
   public void deleteRecipe(Long id){
        for(RecipeEntity rec : recipes){
            if(rec.getId().equals(id)){
                recipes.remove(rec);
                break;
            }
        }
    }
    public void deleteFavorite(Long id){
        for(RecipeEntity rec : favotite){
            int i = 0;
            if(rec.getId().equals(id)){
                favotite.remove(rec);
                break;
            }
            i++;
        }
    }
    public void addFavorite(RecipeEntity recipeEntity){
       favotite.add(recipeEntity);
    }
    //Security
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return activated;
    }
}
