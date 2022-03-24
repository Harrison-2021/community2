package com.nowcoder.community.service;

import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
    public void like(int userId, int entityType, int entityId, int entityUserId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 在事务启动前查询
                Boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                // 启动事务
                operations.multi();
                if(isMember) {  // 已经点过赞了，再次点赞，就是取消赞
                    operations.opsForSet().remove(entityLikeKey, userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                return operations.exec();   // 提交事务
            }
        });

    }

    /**
     * 查询指定实体的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return         用数字来表示点赞的状态，方便扩展
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String key = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(key, userId) ? LIKE_STATUS_YES : LIKE_STATUS_NO;
    }

    /**
     * 查询指定实体的点赞数量
     * @param entityType
     * @param entityId
     * @return  注意返回类型是long类型
     */
    public long findEntityLikeCount(int entityType, int entityId) {
        return redisTemplate.opsForSet().size(RedisKeyUtil.getEntityLikeKey(entityType, entityId));
    }

    /**
     * 查询指定用户获取的点赞数量
     * @param userId    指定用户的id
     * @return          int类型
     */
    public int findUserLikeCount(int userId) {
        // 注意类型的转换-value为String类型，要转为int类型
        Integer count = (Integer)redisTemplate.opsForValue().get(RedisKeyUtil.getUserLikeKey(userId));
        return count == null ? 0 : count.intValue();
    }
}
