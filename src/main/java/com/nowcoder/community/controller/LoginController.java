package com.nowcoder.community.controller;

import com.google.code.kaptcha.Producer;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.security.acl.Owner;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class LoginController implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    /**
     * 注册页面的显示
     * @return
     */
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage() {
        return "/site/register";
    }

    /**
     * 登录页面显示
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage() {
        return "/site/login";
    }


    /**
     * 注册页面表单提交请求
     * @param model
     * @param user
     * @return
     */
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

    /**
     * 激活账号的请求
     * http://localhost:8080/community/activation/101/code
     * @param model
     * @param userId
     * @param code  激活码
     * @return
     */
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
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/) {
        // 生成验证码
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

       /* 将验证码存入session中
        session.setAttribute("kaptcha", text);*/
        // 将验证码改为存入redis中
        // 先创建验证码的归属-向客户端浏览器中存cookie
        Cookie cookie = new Cookie("kaptchaOwner", CommunityUtil.generateUUID());
        cookie.setMaxAge(60);
        cookie.setPath(contextPath);
        response.addCookie(cookie); // 添加进响应头，就会自动发送给浏览器
        // 创建redis的key，并储存验证码字符串
        String kaptchaKey = RedisKeyUtil.getKaptcha(cookie.getValue());
        redisTemplate.opsForValue().set(kaptchaKey, text, 60, TimeUnit.SECONDS); // 方法重载：过期时间

        // 将服务器中生成的图片直接输出写给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image, "png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败：" + e.getMessage());
        }
    }

    /**
     * 登录请求表单处理
     * @param model
     * @param username
     * @param password
     * @param code          用户输入的验证码
     * @param rememberMe    记住我，设置登录凭证生效时间
     * @param response
     * @return
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(Model model, String username, String password, String code,
                        boolean rememberMe, HttpServletResponse response,/* HttpSession session*/
                        /*@CookieValue("kaptchaOwner") String owner,*/HttpServletRequest request) {
        // 1.先验证表单页面的验证码信息
        // 从session中获取验证码字符串
        //String kaptcha = (String)session.getAttribute("kaptcha");
        // 改从redis中取-注意有可能cookie过期，因此要判空
        String kaptcha = null;
        String owner = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals("kaptchaOwner")) {
                owner = cookie.getValue();
                break;
            }
        }
        if(StringUtils.isNotBlank(owner)) {
            String kaptchakey = RedisKeyUtil.getKaptcha(owner);
            kaptcha = (String)redisTemplate.opsForValue().get(kaptchakey);
        } else {
            model.addAttribute("codeMsg", "验证码过期，请重新获取!");
            return "/site/login";
        }
        // 与用户输入验证码比对，注意要判空
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code)
            || !kaptcha.equalsIgnoreCase(code)) {
            model.addAttribute("codeMsg", "验证码不正确!");
            return "/site/login";
        }

        // 2. 验证账号、密码，验证成功后，记得向浏览器颁发登录凭证ticket
        // 生成登录凭证需要，先读取用户选择的登录凭证生效时间，从常量接口中获取
        int expiredSeconds = rememberMe ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String, Object> map = userService.login(username, password, expiredSeconds);
        // 验证成功后，记得向浏览器颁发登录凭证ticket
        if(map.containsKey("ticket")) { // 验证成功，才能发送ticket，不是map为null
            // 创建cookie储存ticket
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            // 设置cookie范围，时间
            cookie.setPath(contextPath);
            // cookie是以秒为单位计时间的，但java中的Date类是以毫秒为单位计时间的
            cookie.setMaxAge(expiredSeconds); // 以凭证的有效时间来定cookie存活时间
            // 向浏览器响应cookie
            response.addCookie(cookie);
            // 重定向到主页
            return "redirect:/index";
        } else { // 验证失败，将错误信息装进model，返回给模板页面，并返回给模板页面
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }
    }

    /**
     * 退出登录请求
     * @param ticket    登录凭证t票
     * @return
     */
    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket) {
        userService.logout(ticket);
        return "redirect:/login";
    }
}
