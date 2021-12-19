package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping(path = "/user")
public class UserController {

    private static Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage()
    {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model)
    {
        if(headerImage==null)
        {
            model.addAttribute("error","您还没选择图片");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        String originalFilename = headerImage.getOriginalFilename();
        String  suffix=originalFilename.substring(originalFilename.lastIndexOf("."));
        if (suffix==null)
        {
            model.addAttribute("error","上传的图片格式有误");
            return "/site/setting";
        }
        //随机生成文件名
        String filename= CommunityUtil.generateUUID() + suffix;
        //文件存放路径
        String fileDestPath = uploadPath + "/" +filename;
        File file = new File(fileDestPath);
        try {
            headerImage.transferTo(file);
        } catch (IOException e) {
            logger.error("文件上传出错" + e.getMessage());
            throw new RuntimeException("文件上传出错",e);
        }
        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        String headerUrl = domain + contextPath + "/user/header/"+ filename;
        userService.updateHeaderUrl(user.getId(),headerUrl );
        return "redirect:/index";
    }

    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable String fileName, HttpServletResponse response)
    {
        //文件存放路径
        String filePath = uploadPath + "/" + fileName;
        //获取文件后缀
        String suffix = filePath.substring(filePath.lastIndexOf(".")+1);
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(filePath);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    @LoginRequired
    @RequestMapping(path = "/update",method = RequestMethod.POST)
    public String update(Model model,String oldPassword,String newPassword,String confirmPassword)
    {
        if(StringUtils.isBlank(oldPassword))
        {
            model.addAttribute("oldPasswdMsg","原密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword))
        {
            model.addAttribute("newPasswdMsg","新密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(confirmPassword))
        {
            model.addAttribute("confirmPasswdMsg","确认密码不能为空");
            return "/site/setting";
        }
        if(!newPassword.equals(confirmPassword))
        {
            model.addAttribute("confirmPasswdMsg","两次密码输入的不一致");
            return "/site/setting";
        }
        User user = hostHolder.getUser();
        oldPassword=CommunityUtil.md5(oldPassword+user.getSalt());
        if(!oldPassword.equals(CommunityUtil.md5(user.getPassword()+ user.getSalt())))
        {
            model.addAttribute("oldPasswdMsg","原密码错误");
            return "/site/setting";
        }
        newPassword=CommunityUtil.md5(newPassword+user.getSalt());
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        userService.updatePassword(user.getId(),newPassword);
        return  "redirect:/index";
    }


}
