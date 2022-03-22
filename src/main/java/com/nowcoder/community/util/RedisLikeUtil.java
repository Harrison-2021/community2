package com.nowcoder.community.util;

public class RedisLikeUtil {
    // redis字符串拼接符:
    private static final String SPLIT = ":";
    // redis定义点赞的key的前缀
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    /**
     * 某个实体的赞的key
     * like:entity:entityType:entityId -> set(userId)
     * @param entityType    实体类型
     * @param entityId      实体id
     * @return              针对特定实体的赞的key
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }
}
