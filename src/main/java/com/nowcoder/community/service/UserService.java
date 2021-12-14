package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class UserService implements CommunityConstant {



    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    public User findUserById(int id) {
        return userMapper.selectById(id);
    }

    //注册是校验用户数据的
    Map<String,Object> map = new HashMap<>();
    public Map<String,Object> register(User user)
    {
        //空值处理
        if(user==null)
        {
            throw  new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank((user.getUsername())))
        {
            map.put("userNameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank((user.getPassword())))
        {
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if(StringUtils.isBlank((user.getEmail())))
        {
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        //账号验证
        User u = userMapper.selectByName(user.getUsername());
        if(u!=null)
        {
            map.put("userNameMsg","用户名已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if(u!=null)
        {
            map.put("emailMsg","该邮箱已被注册");
            return map;
        }
        //注册用户
        user.setCreateTime(new Date());
        user.setType(0);
        user.setStatus(0);
        //设置随机激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        //http://images.nowcoder.com/head/%dt.png  牛客的图片服务器，图片范围0-1000，新用户随机指定头像
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        //明文密码+随机字符串再md5 加密提高安全性
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        userMapper.insertUser(user);
        //邮箱激活
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        // 邮箱激活路由  http://localhost:8080/community/activation/101/code
        String url = "http://localhost:8080/community/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        //网页模板，以及变量信息并发送激活邮箱
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }

    //激活业务
    public int activation(int userId,String code)
    {
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1)
        {
            return ACTIVATION_REPEAT;
        }
        else if(user.getActivationCode().equals(code))
        {
            userMapper.updateStatus(user.getId(),1);
            return ACTIVATION_SUCCESS;
        }
        else {
            return  ACTIVATION_FAILURE;
        }
    }

    public Map<String,Object> login(String username,String password,long expiredSecond)
    {
        //校验账号、是否激活、密码
        HashMap<String, Object> map = new HashMap<>();
        if(StringUtils.isBlank(username))
        {
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(password))
        {
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        User user = userMapper.selectByName(username);
        if(user==null)
        {
            map.put("usernameMsg","您输入的账号不存在");
            return map;
        }
        if(user.getStatus()!=1)
        {
            map.put("usernameMsg","您的账号未激活，请先激活，激活码已发送至您的邮箱");
            return map;
        }
        password=CommunityUtil.md5(password+user.getSalt());
        if(!password.equals(user.getPassword()))
        {
            map.put("passwordMsg","您输入的密码有误");
            return map;
        }
        //登录-生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        //session 是否有效
        loginTicket.setStatus(0);
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSecond*1000));
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket", loginTicket.getTicket());
        return  map;
    }

    public void logout(String ticket)
    {
        loginTicketMapper.updateStatus(ticket,1);
    }

    public  LoginTicket findLoginTicket(String ticket)
    {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        return  loginTicket;
    }

    public int updateHeaderUrl(int userId,String headerUrl)
    {
        int row = userMapper.updateHeader(userId, headerUrl);
        return row;
    }

    public int updatePassword(int userId,String password)
    {
        int row = userMapper.updatePassword(userId, password);
        return row;
    }
}
