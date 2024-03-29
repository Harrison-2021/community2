package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.event.EventProducer;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    /**
     * 处理点赞的异步请求
     * @param entityType
     * @param entityId
     * @return  json字符串，不传递msg，如果有问题，直接在网页alert提示
     */
    @RequestMapping(value = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        // 用来封装信息的map
        Map<String, Object> map = new HashMap<>();
        // 权限-统一管理，先获取当前用户
        User user = hostHolder.getUser();
        // 点赞事件处理
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // 点赞数量获取
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // 点赞状态获取
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // 统一封装到map传给前端页面
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞事件后-系统向目标用户发送通知
        if(likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setFromUserId(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.sendEvent(event);
        }
        return CommunityUtil.getJSONString(0, null, map);
    }
}
