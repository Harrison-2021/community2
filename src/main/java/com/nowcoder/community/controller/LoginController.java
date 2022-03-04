package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.entity.CommunityConstant;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import org.aspectj.weaver.ast.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;

    @Autowired
    Producer kaptchaProducer;

    // 注册页面的显示
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
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

    // http://localhost:8080/community/activation/101/code
    @RequestMapping(path = "/activate/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code) {
        // 将userId,code解析出来后交给service处理，
        // 接收service反馈的信息
        int result = userService.activation(userId, code);
        if (result == ACTIVATION_SUCCESS) { // 激活成功，跳转到登录页面
            model.addAttribute("msg", "激活成功,您的账号已经可以正常使用了，请登录!");
            model.addAttribute("target", "/login");
        } else if (result == ACTIVATION_REPEAT) { // 重复激活
            model.addAttribute("msg", "无效操作,该账号已经激活过了!");
            model.addAttribute("target", "/index");
        } else {
            model.addAttribute("msg", "激活失败,您提供的激活码不正确!");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    /** 生成验证码图片网页*/
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        // 将验证码存入session中
        session.setAttribute("kaptcha", text);

        // 将服务器中生成的图片直接输出写给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }
}
