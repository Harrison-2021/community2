package com.nowcoder.community.controller;

import com.nowcoder.community.controller.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    /**
     * 处理关注的异步请求
     * @param entityType    关注对象类型
     * @param entityId      关注对象id
     * @return
     */
    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired  // 自定义拦截器，如果没有登录，就不能访问这个请求
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if(user == null) {  // 拦截器已经拦截，若拦截不成功，再次抛出异常
            throw new IllegalArgumentException("用户没有登录!");
        }
        followService.follow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "关注成功!");
    }

    /**
     * 处理取消关注的异步请求
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(value = "/unfollow", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired  // 自定义拦截器，如果没有登录，就不能访问这个请求
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if(user == null) {  // 拦截器已经拦截，若拦截不成功，再次抛出异常
            throw new IllegalArgumentException("用户没有登录!");
        }
        followService.unFollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注!");
    }

}
