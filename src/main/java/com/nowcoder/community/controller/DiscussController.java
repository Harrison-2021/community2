package com.nowcoder.community.controller;

import com.nowcoder.community.controller.annotation.LoginRequired;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

@Controller
@RequestMapping("/discuss")
public class DiscussController {
    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    HostHolder hostHolder;

    /**
     * 处理ajax异步发布帖子请求
     * @param title     帖子主题
     * @param content   帖子内容
     * @return          JSON字符串
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addPost(String title, String content) {
        // 1.先获取当前用户，进行权限判断
        User user = hostHolder.getUser();
        if(user == null) {
            return CommunityUtil.getJSONString(403, "您还没登录，无法发布帖子!");
        }
        // 创建帖子，并调用service层处理
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPostService.insertPost(discussPost);

        // 返回JSON字符串，先处理成功的，失败的今后统一处理
        return CommunityUtil.getJSONString(0, "发布成功!");
    }
}
