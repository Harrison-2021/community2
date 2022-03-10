package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;
    // 查询指定页面信息的帖子列表
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.getPosts(userId, offset, limit);
    }

    // 查询一共有多少条帖子
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.getPostRows(userId);
    }

    /**
     * 添加帖子业务-要对内容进行过滤
     * @param discussPost
     * @return
     */
    public int insertPost(DiscussPost discussPost) {
        // 1.边界处理-先判空
        if(discussPost == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }

        // 2.对标题、帖子内容进行过滤
        // 先进行HTML格式转义
        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        discussPost.setContent(HtmlUtils.htmlEscape(discussPost.getContent()));
        // 再进行敏感词过滤
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));
        discussPost.setContent(sensitiveFilter.filter(discussPost.getContent()));

        // 3.入库
        return discussPostMapper.insertPosts(discussPost);
    }

    /** 查询一条帖子信息 */
    public DiscussPost selectPost(int id) {
        return discussPostMapper.selectPost(id);
    }
}
