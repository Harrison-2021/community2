package com.nowcoder.community.service;

import com.nowcoder.community.dao.CommentMapper;
import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.entity.Comment;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {
    @Autowired
    CommentMapper commentMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    @Autowired
    DiscussPostMapper discussPostMapper;

    /** 根据实体目标类型id分页查询所有评论列表 */
    public List<Comment> selectCommentList(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectCommentList(entityType, entityId, offset, limit);
    }

    /** 查询实体目标类型id下的评论数量 */
    public int selectComments(int entityType, int entityId) {
        return commentMapper.selectComments(entityType, entityId);
    }

    /** 处理添加评论的业务-事务管理-更新帖子评论 */
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        // 先添加评论
        // -注意过滤敏感词
        // -业务层，只处理接受来的原材料进行加工入库和查询，如何创造原材料是上级的逻辑
        if(comment == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int res = commentMapper.insertComment(comment);

        // 在一个事务中，进行更新帖子评论
        // 只更新帖子的评论数量，评论的评论不包括
        if(comment.getEntityType() == ENTITY_TYPE_POST) {
            discussPostMapper.updateCommentCount(comment.getEntityId(),
                    commentMapper.selectComments(ENTITY_TYPE_POST, comment.getEntityId()));
        }

        return res;
    }

    /**
     * 根据评论id查询一条评论
     */
    public Comment selectCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}
