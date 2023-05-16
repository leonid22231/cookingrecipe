package com.lyadev.cookingrecipes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lyadev.cookingrecipes.entity.enums.MessageCodes;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageEntity {

    private int statusCode;
    private String message;
    private Object json;

    public MessageEntity(MessageCodes messageCodes, String message){
        this.statusCode = messageCodes.value();
        this.message = message;
    }
    public MessageEntity(MessageCodes messageCodes, String message, Object object){
        this.statusCode = messageCodes.value();
        this.message = message;
        this.json = object;
    }
    public MessageEntity(MessageCodes messageCodes, Object object){
        this.statusCode = messageCodes.value();
        this.json = object;
    }
}
