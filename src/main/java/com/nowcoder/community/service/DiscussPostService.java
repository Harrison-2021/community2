package com.nowcoder.community.service;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    // 查询指定页面信息的帖子列表
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.getPosts(userId, offset, limit);
    }

    // 查询一共有多少条帖子
    public int findDiscussPostRows(int userId) {
        return discussPostMapper.getPostRows(userId);
    }
}
