package com.nowcoder.community.dao;

import com.nowcoder.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    // 查询指定页面信息的帖子列表
    List<DiscussPost> getPosts(int userId, int offset, int limit);

    // 查询一共有多少条帖子
    // 如果只有一个参数,并且在<if>里使用,则必须加@Param注解
    int getPostRows(@Param("userId") int userId);

    /**
     * 增加帖子
     */
    int insertPosts(DiscussPost discussPost);

    /** 查询一条帖子 */
    DiscussPost selectPost(int id);

}
