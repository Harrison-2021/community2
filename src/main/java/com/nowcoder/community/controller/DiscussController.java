package com.nowcoder.community.controller;

import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.CommentService;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.LikeService;
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

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussController implements CommunityConstant {
    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    CommentService commentService;

    @Autowired
    LikeService likeService;

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

    /**
     * 处理访问一条帖子的详情页面的请求
     * @param model
     * @param postId 帖子id
     * @return
     */
    @RequestMapping(value = "/detail/{postId}", method = RequestMethod.GET)
    public String getPostPage(Model model, @PathVariable("postId") int postId, Page page ) {
// 1.帖子的数据信息
        // 查询到指定帖子-由于点击的是帖子主题，一定存在帖子
        DiscussPost discussPost = discussPostService.selectPost(postId);
        model.addAttribute("post", discussPost);
        // 查询到帖子作者信息-帖子必须由指定用户发布，故，一定能查询到用户
        User user = userService.findUserById(discussPost.getUserId());
        model.addAttribute("user", user);
        // 查询帖子的点赞信息-状态和数量
        // 检查是否登录，未登录状态，一定显示未点赞状态
        int likeStatus = hostHolder.getUser() == null ? LIKE_STATUS_NO :
                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, postId);
        model.addAttribute("likeStatus", likeStatus);
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);
        model.addAttribute("likeCount", likeCount);

        // 处理分页信息-设置页面
        page.setLimit(5);
        page.setPath("/discuss/detail/" + postId);
        page.setRows(discussPost.getCommentCount());    // 直接从帖子表中拿，减少查询次数

// 2.对帖子的评论-数据信息
        // 查询帖子的评论分页信息-将关联用户信息一并封装
        List<Comment> commentList = commentService.selectCommentList(
                ENTITY_TYPE_POST, discussPost.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if(commentList != null) {   // 有可能没有评论，注意边界条件
            for(Comment comment : commentList) {
                Map<String, Object> commentVoMap = new HashMap<>();
                // 评论信息
                commentVoMap.put("comment", comment);
                // 评论作者信息
                commentVoMap.put("user", userService.findUserById(comment.getUserId()));
                // 对评论的点赞信息-状态和数量
                // 检查是否登录，未登录状态，一定显示未点赞状态
                likeStatus = hostHolder.getUser() == null ? LIKE_STATUS_NO :
                        likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVoMap.put("likeStatus", likeStatus);
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVoMap.put("likeCount", likeCount);
// 3. 对评论的回复-数据信息
                // 每条评论的回复数量统计
                int replyCount = commentService.selectComments(ENTITY_TYPE_COMMENT, comment.getId());
                commentVoMap.put("replyCount", replyCount);
                // 根据每条评论的id，循环查询每条评论下的所有回复信息
                List<Comment> replyList = commentService.selectCommentList(
                        ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE); // 回复显示无需分页
                List<Map<String, Object>> replyVoList = new ArrayList<>(); // 封装list
                if(replyList != null) {
                    for(Comment reply : replyList) {
                        Map<String, Object> replyVoMap = new HashMap<>();
                        replyVoMap.put("reply", reply);
                        replyVoMap.put("user", userService.findUserById(reply.getUserId()));
// 4.对回复的回复-数据信息
                        // 普通回复中需要用到targetId
                        // 找到回复目标用户作者-注意，如果targetId=0,表示无效，无需查询
                        User targetUser = reply.getTargetId() == 0 ? null :
                                userService.findUserById(reply.getTargetId());
                        replyVoMap.put("targetUser", targetUser);

                        // 对回复的点赞信息-状态和数量
                        // 检查是否登录，未登录状态，一定显示未点赞状态
                        likeStatus = hostHolder.getUser() == null ? LIKE_STATUS_NO :
                                likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVoMap.put("likeStatus", likeStatus);
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVoMap.put("likeCount", likeCount);

                        replyVoList.add(replyVoMap);
                    }
                }

                commentVoMap.put("replys",replyVoList); // 将当前评论的所有回复封装数据放进评论map中

                commentVoList.add(commentVoMap);
            }
        }
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }
}
