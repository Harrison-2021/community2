package com.nowcoder.community.service;

import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.RedisKeyUtil;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPSize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements CommunityConstant {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserMapper userMapper;

    /**
     * 关注实体事件-被关注对象的粉丝信息也同时更新-增加redis事件处理
     *
     * @param userId     当前用户id
     * @param entityType 关注对象类型
     * @param entityId   关注对象id
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
     *
     * @param userId     当前用户id
     * @param entityType 关注对象类型
     * @param entityId   关注对象id
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
     *
     * @param userId
     * @param entityType
     * @return 返回long类型
     */
    public long findFollowTargetCnt(int userId, int entityType) {
        String followTarget = RedisKeyUtil.getFollowTarget(userId, entityType);
        return redisTemplate.opsForZSet().size(followTarget);
    }

    /**
     * 获取当前用户的粉丝数
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public long findFollowFansCnt(int entityType, int entityId) {
        String followFans = RedisKeyUtil.getFollowFans(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followFans);
    }

    /**
     * 判断当前用户userid是否关注了目标对象entityId-通过判断目标对象的粉丝中有无当前对象
     *
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followTargetKey = RedisKeyUtil.getFollowTarget(userId, entityType);
        return redisTemplate.opsForZSet().score(followTargetKey, entityId) != null;
    }

    /**
     * 获取关注目标对象列表信息-支持分页查询
     * 放进list列表中，每个列表将查询到的value中的member，score用map封装好
     * @param userId    指定用户的id-类型统一为用户
     * @return
     */
    public List<Map<String, Object>> followList(int userId, int offset, int limit) {
        List<Map<String, Object>> list = new ArrayList<>();
        String followKey = RedisKeyUtil.getFollowTarget(userId, ENTITY_TYPE_USER);

        // 倒序查询指定范围的value
        // 虽返回的是有序集合，框架内部对set集合做了有序的处理
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(followKey, offset, offset + limit - 1);
        // 边界处理
        if(targetIds == null) {
            return null;
        }
        // 将获取的value中的member与score分别拿出放进map中
        addData(targetIds, followKey, list);
        return list;
    }

    /**
     * 查询粉丝类别-封装的数据变量名最好保存一致，方便今后封装-统一处理
     * @param userId    指定用户的id-类型为用户
     * @param offset
     * @param limit
     * @return
     */
    public List<Map<String, Object>> fansList(int userId, int offset, int limit) {
        String fansKey = RedisKeyUtil.getFollowFans(ENTITY_TYPE_USER, userId);
        Set<Integer> targetIds = redisTemplate.opsForZSet().reverseRange(fansKey, offset, offset + limit - 1);

        if (targetIds == null) {
            return null;
        }

        List<Map<String, Object>> list = new ArrayList<>();
        // 封装数据
        addData(targetIds, fansKey, list);
        return list;
    }

    /**
     * 封装zSet中的value数据函数
     * @param targetIds      zSet的value
     * @param key            zSet的key
     * @param list           装有map的列表
     */
    private void addData(Set<Integer> targetIds, String key, List<Map<String, Object>> list) {
        for (Integer targetId : targetIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userMapper.selectById(targetId);
            map.put("user", user);
            Double score = redisTemplate.opsForZSet().score(key, targetId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
    }
}
