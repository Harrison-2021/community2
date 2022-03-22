package com.nowcoder.community.service;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisLikeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LikeService implements CommunityConstant {
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 添加赞或删除赞，根据当前用户点赞的状态来定
     * @param userId        当前访问用户id
     * @param entityType    访问的实体类型
     * @param entityId      访问的实体id
     */
    public void like(int userId, int entityType, int entityId) {
        String key = RedisLikeUtil.getEntityLikeKey(entityType, entityId);
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId);
        if(isMember) {
            redisTemplate.opsForSet().remove(key, userId);
        } else {
            redisTemplate.opsForSet().add(key, userId);
        }
    }

    /**
     * 查询指定实体的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return         用数字来表示点赞的状态，方便扩展
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String key = RedisLikeUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key, userId) ? LIKE_STATUS_YES : LIKE_STATUS_NO;
    }

    /**
     * 查询指定实体的点赞数量
     * @param entityType
     * @param entityId
     * @return  注意返回类型是long类型
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        return redisTemplate.opsForSet().size(RedisLikeUtil.getEntityLikeKey(entityType, entityId));
    }
}
