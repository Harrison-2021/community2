package com.nowcoder.community.controller;

import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController {
    @Autowired
    private UserService userService;

    // 注册页面的显示
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    // 注册页面表单提交请求
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user) {
        // 获取服务端的注册信息
        Map<String, Object> falseMap = userService.register(user);

        // 前端显示处理这些信息
        if(falseMap == null || falseMap.isEmpty()) {
            // 注册成功，封装中间跳转页面需要的信息
            // 提示注册成功，已经向用户发送了激活邮件，提醒用户尽快激活账户
            model.addAttribute("msg", "注册成功，已经向您的邮箱发送了一封激活邮件，请尽快激活!");
            //一定时间后，自动跳转或立即手动跳转的指定链接
            model.addAttribute("target", "/index");
            return "/site/operate-result"; // 注册成功跳转到激活提示激活页面
        } else {
            model.addAttribute("usernameMsg", falseMap.get("usernameMsg"));
            model.addAttribute("passwordMsg", falseMap.get("passwordMsg"));
            model.addAttribute("emailMsg", falseMap.get("emailMsg"));
            return "/site/register"; // 返回注册表单页面
        }
    }
}
