package com.lyadev.cookingrecipes.entity.enums;

public enum MessageCodes implements Codes{
    OK(10100),
    USER_EXISTS(10200),
    CODE_SENDED(10300),
    USER_CREATED(10400),
    CODE_WRONG(10500),
    PASSWORD_WRONG(10600),
    CODE_NOT_FOUND(10700),
    USER_NOT_FOUND(10800),
    EMAIL_WRONG(10900),
    INFO_CHANGED(11000),
    INFO_NOT_CHANGED(11100),
    PASSWORD_CHANGED(11200),
    USER_DELETED(11300),
    RECIPE_NOT_FOUND(11400),
    COMMENT_NOT_FOUND(11500),
    ERROR(11111);
    private int code;

    MessageCodes(int value) {
        this.code = value;
    }

    public int getCode() {
        return code;
    }

    @Override
    public int value() {
        return code;
    }
}
