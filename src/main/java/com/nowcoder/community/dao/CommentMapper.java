package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    /** 根据实体目标类型id分页查询所有评论列表 */
    List<Comment> selectCommentList(int entityType, int entityId, int offset, int limit);

    /** 查询实体目标类型id下的评论数量 */
    int selectComments(int entityType, int entityId);

    /** 增加评论 */
    int insertComment(Comment comment);

}
