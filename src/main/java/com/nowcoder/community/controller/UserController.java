package com.nowcoder.community.controller;

import com.nowcoder.community.controller.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import com.sun.org.apache.xpath.internal.operations.Mod;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

@Controller
@RequestMapping("/user")
public class UserController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    LikeService likeService;

    @Value("${community.path.domain}")
    String domain;

    @Value("${server.servlet.context-path}")
    String contextPath;

    @Value("${community.path.upload}")
    String uploadPath;

    /** 显示账号设置页面 */
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    /** 处理用户头像文件上传请求 */
    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    // Spring MVC 中的MultipartFile接口会自动获取上传的文件图片
    public String uploadHeader(MultipartFile headerImage, Model model) {
        System.out.println(headerImage);
        if(headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        // 对上传的文件进行处理
        // 先读取文件名，存入本地前，为防止用户上传文件名重复，需重命名：随机字符串.后缀
        String filename = headerImage.getOriginalFilename();
        System.out.println(filename);
        // 读取后缀,并验证后缀名是否合法
        String suffix = filename.substring(filename.lastIndexOf(".")); // .字符开始往后
        if(StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件格式不正确!");
            return "/site/setting";
        }
        filename = CommunityUtil.generateUUID() + suffix;

        // 定义文件存放的地址
        String address = uploadPath + "/" + filename;
        File dest = new File(address); // 指定地址要存放接受图片的空文件
        // 将上传文件写入到指定路径文件中
        try {
            headerImage.transferTo(dest);
        } catch (IOException e) {
           logger.error("上传文件失败!" + e.getMessage());
            throw new IllegalArgumentException("上传文件失败，服务器发生异常!", e);
        }

        // 上传头像成功后，要将用户头像的数据更改
        // 要给新的用户头像一个web能访问的url地址
        // http://localhost:8080/community/user/header/xxx.png
        String headerUrl = domain + contextPath + "/user/header" + "/" + filename;
        User user = hostHolder.getUser();
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    /** 获取用户头像请求,访问路径即之前设置的路径，服务端一段从内存文件里读，一端写给浏览器*/
    @RequestMapping(value = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename,
                            HttpServletResponse response) {
        // 1. 获取服务器存放路径的文件
        filename = uploadPath + "/" + filename;
        // 2. 因response要向浏览器响应图片，需要类型，故需要读取后缀
        String suffix = filename.substring(filename.lastIndexOf("."));
        // 3. 向浏览器响应图片
        response.setContentType("image/" + suffix); // image/.png,也可以识别
        try (
                FileInputStream fis = new FileInputStream(filename);
                OutputStream os = response.getOutputStream();
        ){
            byte[] buffer = new byte[1024];
            int b = 0;
            while((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }

    /**
     * 访问指定用户个人主页的请求
     * @param userId    指定的用户id
     * @param model
     * @return
     */
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        // 先获取要访问的用户
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new IllegalArgumentException("该用户不存在!");
        }
        // 将用户信息封装
        model.addAttribute("user", user);
        // 获取用户的点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        return "/site/profile";
    }
}
