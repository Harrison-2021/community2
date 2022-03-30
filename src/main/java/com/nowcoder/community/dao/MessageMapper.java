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

    /** 新增消息 */
    int insertMessage(Message message);

    /**
     * 更新私信已读状态
     * @param ids       要更新的一组私信id
     * @param status    要更改的状态
     * @return
     */
    int updateStatus(List<Integer> ids, int status);

    /** 查询系统给指定用户指定主题下最新的通知*/
    Message selectLatestNotice(int userId, String topic);

    /**
     * 查询系统给指定用户指定主题下包含系统通知的数量
     */
    int selectNoticeCount(int userId, String topic);

    /**
     * 查询未读系统通知的数量-动态传参
     * 如果传入主题，就是当前主题未读数量-否则就是所有主题的未读消息
     * @param userId
     * @param topic
     * @return
     */
    int selectNoticeUnreadCount(int userId, String topic);

    /**
     * 查询指定主题下的通知列表详情
     * @param userId
     * @param topic
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectNoticeList(int userId, String topic, int offset, int limit);

}
