package com.nowcoder.community.dao;

import com.nowcoder.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {
    /** 查询指定用户的会话列表 -内容只显示最新一条私信*/
    List<Message> selectConversationList(int userId, int offset, int limit);

    /** 查询指定用户会话总数 */
    int selectConversationCount(int userId);

    /** 查询指定会话的两个用户之间的私信列表 */
    List<Message> selectLetterList(String conversationId, int offset, int limit);

    /** 查询指定会话的两个用户之间的私信总数 */
    int selectLetterCount(String conversationId);

    /** 查询未读私信数量-动态条件查询-当前用户总的未读私信数量-当前会话的未读私信数量 */
    int selectUnreadCount(int userId, String conversationId);
}
