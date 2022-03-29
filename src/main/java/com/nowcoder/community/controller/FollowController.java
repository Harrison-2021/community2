package com.nowcoder.community.controller;

import com.nowcoder.community.controller.annotation.LoginRequired;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {
    @Autowired
    FollowService followService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    EventProducer eventProducer;

    /**
     * 处理关注的异步请求
     * @param entityType    关注对象类型
     * @param entityId      关注对象id
     * @return
     */
    @RequestMapping(value = "/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if(user == null) {  // 拦截器已经拦截，若拦截不成功，再次抛出异常
            throw new IllegalArgumentException("用户没有登录!");
        }
        followService.follow(user.getId(), entityType, entityId);
        // 触发关注事件-系统向关注对象发送通知
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setFromUserId(hostHolder.getUser().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId); // 目前关注的都是人，因此实体类型都是User
        eventProducer.sendEvent(event);
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
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        if(user == null) {  // 拦截器已经拦截，若拦截不成功，再次抛出异常
            throw new IllegalArgumentException("用户没有登录!");
        }
        followService.unFollow(user.getId(), entityType, entityId);
        return CommunityUtil.getJSONString(0, "已取消关注!");
    }


    /**
     * 处理访问关注对象的列表
     * @param userId    指定用户的id-查询指定用户关注的人
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(value = "/followee/{userId}", method = RequestMethod.GET)
    public String getFollowerList(@PathVariable("userId") int userId, Page page, Model model) {
        // 先查询和判断用户
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        // 设置页面
        page.setPath("/followee/" + userId);
        page.setLimit(5);
        page.setRows((int)followService.findFollowTargetCnt(userId, ENTITY_TYPE_USER));

        // 查询列表，并将数据传给前端
        // 数据列表中基本信息都在service层包装好了，只要传递过来就可以
        List<Map<String, Object>> userList = followService.followList(userId, page.getOffset(), page.getLimit());
        // 需要另外判断每个列表中关注的用户的关注状态，因需要登录用户信息，故需要在controller层处理
        addStatus(userList);
        model.addAttribute("users", userList);
        return "/site/followee";
    }

    /**
     * 处理访问粉丝列表的请求
     * @param userId    指定用户的id-查询指定用户关注的人
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(value = "/fans/{userId}", method = RequestMethod.GET)
    public String getFansList(@PathVariable("userId") int userId, Page page, Model model) {
        // 先查询和判断用户
        User user = userService.findUserById(userId);
        if(user == null) {
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);

        // 设置页面
        page.setPath("/fans/" + userId);
        page.setLimit(5);
        page.setRows((int)followService.findFollowFansCnt(ENTITY_TYPE_USER, userId));

        // 查询列表，并将数据传给前端
        // 数据列表中基本信息都在service层包装好了，只要传递过来就可以
        List<Map<String, Object>> userList = followService.fansList(userId, page.getOffset(), page.getLimit());
        // 需要另外判断每个列表中关注的用户的关注状态，因需要登录用户信息，故需要在controller层处理
        addStatus(userList);
        model.addAttribute("users", userList);
        return "/site/follower";
    }

    /**
     * 在列表中的每个用户添加上关注的状态信息
     * @param userList
     */
    private void addStatus(List<Map<String, Object>> userList) {
        if(userList != null) {
            for(Map<String, Object> map : userList) {
                User u = (User) map.get("user");
                // 判断是否关注，需要验证访问用户登录信息，多处使用，封装起来
                map.put("hasFollowed", hasFollowed(u.getId()));
            }
        }
    }

    /**
     * 关注状态判断
     * @param userId
     * @return
     */
    private boolean hasFollowed(int userId) {
        if(hostHolder.getUser() == null) {
            return false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }
}
