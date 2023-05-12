package com.lyadev.cookingrecipes.service;

import com.lyadev.cookingrecipes.entity.UserEntity;
import com.lyadev.cookingrecipes.entity.UserTokenEntity;
import com.lyadev.cookingrecipes.repository.UserTokenRepository;
import lombok.AllArgsConstructor;
import org.hibernate.type.descriptor.DateTimeUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class UserTokenService {
    private UserTokenRepository userTokenRepository;
    private SimpleEmailService simpleEmailService;
    private UserService userService;

    public String getTokenById(Long id){
        return userTokenRepository.findByUser_id(id).getToken();
    }
    public UserTokenEntity getTokenByUser(UserEntity userEntity){
        return userTokenRepository.findByUser_id(userEntity.getId());
    }
    public void deleteByUser(UserEntity userEntity){
        userTokenRepository.delete(userTokenRepository.findByUser_id(userEntity.getId()));
    }
    public void CheckState(){
       List<UserTokenEntity> userTokenEntities = userTokenRepository.findAll();
       if(userTokenEntities.size()>0){
           for(UserTokenEntity ent : userTokenEntities){
              Date date =  ent.getCreatedate();
              Date date_now = Calendar.getInstance().getTime();
              Date end_date = new Date(date.getTime() + (60000 * 30));
              if(end_date.getTime() > date_now.getTime()){
                  TimerTask timerTask = new TimerTask(){
                      @Override
                      public void run() {
                          deleteByUser(ent.getUserid());
                          if(!ent.getUserid().isEnabled())
                          userService.delete(ent.getUserid().getId());
                          System.out.println("DELETED["+ent.getId()+"] - " + Calendar.getInstance().getTime().getTime());
                      }

                  };
                  Timer timer = new Timer();
                  timer.schedule(timerTask,end_date.getTime()-date_now.getTime());
              }if(end_date.getTime()<date_now.getTime()){
                  userTokenRepository.delete(ent);
                  userService.delete(ent.getUserid().getId());
               }
           }
       }
    }
    public void generateTokenByUser(UserEntity userEntity){
        if(userTokenRepository.findByUser_id(userEntity.getId())==null) {
            UserTokenEntity userTokenEntity = new UserTokenEntity();
            userTokenEntity.setUserid(userEntity);
            userTokenEntity.setCreatedate(Calendar.getInstance().getTime());
            Random r = new Random();
            List<Integer> codes = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                int x = r.nextInt(9999);
                while (codes.contains(x))
                    x = r.nextInt(9999);
                codes.add(x);
            }
            String str = String.format("%04d", codes.get(0));
            userTokenEntity.setToken(str);
            userTokenRepository.save(userTokenEntity);

            simpleEmailService.send(userEntity.getEmail(), str);
            TimerTask timerTask;
if(userEntity.isEnabled()){
    timerTask = new TimerTask() {
        @Override
        public void run() {
            deleteByUser(userEntity);
        }

    };
}else {
   timerTask  = new TimerTask() {
        @Override
        public void run() {
            deleteByUser(userEntity);
            userService.delete(userEntity.getId());
        }

    };
}

            Timer timer = new Timer();
            timer.schedule(timerTask, 60000 * 30);
        }
    }
}
