package com.nowcoder.community.util;

public class RedisLikeUtil {
    // redis字符串拼接符:
    private static final String SPLIT = ":";
    // redis定义点赞的key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity"; // 对实体的点赞
    private static final String PREFIX_USER_LIKE = "like:user";     // 对用户的点赞

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
}
