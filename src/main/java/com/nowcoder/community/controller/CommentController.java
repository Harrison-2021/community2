package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.HostHolder;
import com.nowcoder.community.event.EventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements CommunityConstant {
    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    DiscussPostService discussPostService;

    /**
     * 针对特定帖子发布评论
     * @param discussPostId     // 目标帖子id
     * @param comment           // 评论内容
     * @return
     */
    @RequestMapping(value = "/add/{discussPostId}", method = RequestMethod.POST)
    public String addComment(@PathVariable("discussPostId") int discussPostId, Comment comment) {
        // 除请求中需要写的评论内容外，需提供其他素材
        comment.setCreateTime(new Date());
        comment.setUserId(hostHolder.getUser().getId());
        comment.setStatus(0);
        // 将数据交给service处理
        commentService.addComment(comment);

        // 添加完评论后，系统向目标用户发送通知-触发评论事件
        // 封装评论事件信息
        Event event = new Event()
                .setTopic(TOPIC_COMMENT)
                .setFromUserId(hostHolder.getUser().getId())
                .setEntityType(comment.getEntityType()) // 评论的可以是帖子，回帖，回复
                .setEntityId(comment.getEntityId())
                .setData("postId", discussPostId);  // 当前评论所属的帖子id
        // 事件对象的作者-分情况判定-帖子作者、评论作者
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            DiscussPost target = discussPostService.selectPostById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment target = commentService.selectCommentById(comment.getEntityId());
            event.setEntityUserId(target.getUserId());
        }
        // 将信息发送到消息队列中
        eventProducer.sendEvent(event);

        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // 触发发帖事件
            event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setFromUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_POST)
                    .setEntityId(discussPostId);
            eventProducer.sendEvent(event);
        }
        return "redirect:/discuss/detail/" + discussPostId;
    }


}
