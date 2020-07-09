package com.miaosha;

import com.miaosha.dao.UserDOMapper;
import com.miaosha.dao.UserPasswordDOMapper;
import com.miaosha.dataobject.UserDO;
import com.miaosha.dataobject.UserPasswordDO;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
//將启动类当成可以支持自动配置的bean
@SpringBootApplication(scanBasePackages = {"com.miaosha"})
@RestController
public class App
{
//    @Autowired
//    private UserPasswordDOMapper userPasswordDOMapper;
//
//
//    @RequestMapping("/hello")
//    public String home(){
//        UserPasswordDO userPasswordDO= userPasswordDOMapper.selectByUserId(1);
//        if(userPasswordDO == null){
//            return "不存在";
//        }else{
//            return userPasswordDO.getEncrptPassword();
//        }
//    }


    public static void main( String[] args )
    {
        SpringApplication.run(App.class,args);
    }
}
