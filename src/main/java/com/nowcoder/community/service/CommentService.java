package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.entity.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    CommentMapper commentMapper;

    /** 根据实体目标类型id分页查询所有评论列表 */
    public List<Comment> selectCommentList(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentList(entityType, entityId, offset, limit);
    }

    /** 查询实体目标类型id下的评论数量 */
    public int selectComments(int entityType, int entityId) {
        return commentMapper.selectComments(entityType, entityId);
    }
}
