package com.nowcoder.community.util;

public class RedisKeyUtil {
    // redis字符串拼接符:
    private static final String SPLIT = ":";
    // redis定义点赞的key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity"; // 对实体的点赞
    private static final String PREFIX_USER_LIKE = "like:user";     // 对用户的点赞
    private static final String PREFIX_FOLLOW_TARGET = "follow:target";   // 关注的对象
    private static final String PREFIX_FOLLOW_FANS = "follow:fans";   // 粉丝

    /**
     * 对某个实体的赞的key
     * like:entity:entityType:entityId -> set(userId)
     * @param entityType    实体类型
     * @param entityId      实体id
     * @return              针对特定实体的赞的key
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户收到的赞的key
     * @param userId    目标用户id
     * @return          针对特定用户的key
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某个用户关注的实体,要保存关注的实体类型，还要保存是谁关注的，方便被关注者统计，
     * 但也要指明被关注的对象具体id，放入value
     * follow:target:userId:entityType -> zset(entityId,now)
     * @param userId        当前用户的id
     * @param entityType    当前用户关注对象的实体类型
     * @return              储存关注对象信息的key
     */
    public static String getFollowTarget(int userId, int entityType) {
        return PREFIX_FOLLOW_TARGET + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体的粉丝
     * follow:fans:entityType:entityId -> zset(userId,now)
     * @param entityType    要储存的实体类型
     * @param entityId      要储存的实体id
     * @return              返回储存实体类型粉丝信息的key
     */
    public static String getFollowFans(int entityType, int entityId) {
        return PREFIX_FOLLOW_FANS + SPLIT + entityType + SPLIT + entityId;
    }
}
