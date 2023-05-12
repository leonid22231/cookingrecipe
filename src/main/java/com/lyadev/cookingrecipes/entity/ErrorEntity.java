package com.lyadev.cookingrecipes.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorEntity {
    private int statusCode;
    private String message;

    public ErrorEntity(int statusCode, String message){
        this.message = message;
        this.statusCode = statusCode;
    }
}
