package com.nowcoder.community.util;

public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;

    /** 默认状态的登录凭证的超时时间, 单位为秒*/
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12; // 12小时

    /** 记住状态的登录凭证超时时间, 单位为秒*/
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 100; // 100天

    /**
     * entityType类型：帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * entityType类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * entityType类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 点赞状态：未赞
     */
    int LIKE_STATUS_NO = 0;

    /**
     * 点赞状态：已赞
     */
    int LIKE_STATUS_YES = 1;

    /**
     * kafka主题-事件：帖子-评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * kafka主题-事件：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * kafka主题-事件：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 事件类型，帖子
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 系统用户id
     */
    int SYSTEM_USER_ID = 1;
}
