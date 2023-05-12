package com.lyadev.cookingrecipes.component;

import com.lyadev.cookingrecipes.service.UserTokenService;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RunAfterStartup {
UserTokenService userTokenService;
    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
       userTokenService.CheckState();
    }
}
