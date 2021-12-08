package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.jws.WebParam;
import java.util.Map;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/register",method = RequestMethod.GET)
    public  String getRegisterPage()
    {
        return "/site/register";
    }

    @RequestMapping(path = "/register",method = RequestMethod.POST)
    public  String register(Model model, User user)
    {
        Map<String, Object> map = userService.register(user);
        if(map==null || map.isEmpty())
        {
            model.addAttribute("msg","注册成功，我们已向您的邮箱发送了一封激活邮件，请尽快激活！");
            //用于跳转首页
            model.addAttribute("target", "/index");
            return  "/site/operate-result";
        }
        else
        {
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            model.addAttribute("emailMsg", map.get("emailMsg"));
            return "/site/register";
        }
    }

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activation/{userId}/{code}")
    public  String activation(Model model, @PathVariable("userId")int userId, @PathVariable("code")String code)
    {

    }
}
