package com.nowcoder.community.service;

import com.nowcoder.community.util.RedisKeyUtil;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class FollowService {
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 关注实体事件-被关注对象的粉丝信息也同时更新-增加redis事件处理
     * @param userId        当前用户id
     * @param entityType    关注对象类型
     * @param entityId      关注对象id
     */
    public void follow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followTargetKey = RedisKeyUtil.getFollowTarget(userId, entityType);
                String followFans = RedisKeyUtil.getFollowFans(entityType, entityId);

                // 开启事务
                operations.multi();
                // 储存关注对象信息-关注对象的粉丝信息
                operations.opsForZSet().add(followTargetKey, entityId, System.currentTimeMillis());
                operations.opsForZSet().add(followFans, userId, System.currentTimeMillis());
                // 提交事务并返回
                return operations.exec();
            }
        });
    }

    /**
     * 取消关注实体事件-被关注对象的粉丝信息也同时更新-增加redis事件处理
     * @param userId        当前用户id
     * @param entityType    关注对象类型
     * @param entityId      关注对象id
     */
    public void unFollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followTargetKey = RedisKeyUtil.getFollowTarget(userId, entityType);
                String followFans = RedisKeyUtil.getFollowFans(entityType, entityId);

                // 开启事务
                operations.multi();
                // 储存关注对象信息-关注对象的粉丝信息
                operations.opsForZSet().remove(followTargetKey, entityId);
                operations.opsForZSet().remove(followFans, userId);
                // 提交事务并返回
                return operations.exec();
            }
        });
    }

    /**
     * 获取当前用户指定类型关注对象的数量
     * @param userId
     * @param entityType
     * @return     返回long类型
     */
    public long findFollowTargetCnt(int userId, int entityType) {
        String followTarget = RedisKeyUtil.getFollowTarget(userId, entityType);
        return redisTemplate.opsForZSet().size(followTarget);
    }

    /**
     * 获取当前用户的粉丝数
     * @param entityType
     * @param entityId
     * @return
     */
    public long findFollowFans(int entityType, int entityId) {
        String followFans = RedisKeyUtil.getFollowFans(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followFans);
    }

    /**
     * 判断当前用户userid是否关注了目标对象entityId-通过判断目标对象的粉丝中有无当前对象
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followTargetKey = RedisKeyUtil.getFollowTarget(userId, entityType);
        return redisTemplate.opsForZSet().score(followTargetKey, entityId) != null;
    }
}
