package com.lyadev.cookingrecipes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class SimpleEmailService {
    @Autowired
     JavaMailSender emailSender;
    public void send(String email, String code) throws MailException {
        SimpleMailMessage simpleMessage = new SimpleMailMessage();
        simpleMessage.setFrom("lya.dev.test@gmail.com");
        simpleMessage.setTo(email);
        simpleMessage.setSubject("Код подтверждения");
        simpleMessage.setText("Ваш код подтверждения в приложении Cooking Recipes: " + code);
        emailSender.send(simpleMessage);
    }

}
