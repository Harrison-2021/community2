package com.nowcoder.community;

import com.jhlabs.math.RidgedFBM;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {
    @Autowired
    private RedisTemplate redisTemplate;


    // 测试连接
    @Test
    public void contextLoads() {
//       new jedis 对象即可
        Jedis jedis = new Jedis("192.168.181.131", 6379);
        System.out.printf(jedis.ping());
    }

    // 测试redis添加字符串
    @Test
    public void testString() {
        String redisKey = "like:user:111";

        System.out.println(redisTemplate.opsForValue().get(redisKey));
    }

    // redis对哈希表的处理
    @Test
    public void testHashes() {
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "zhangSan");

        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    // 测试redis对list集合的处理
    @Test
    public void testLists() {
        String redisKey = "test:ids";

        redisTemplate.opsForList().leftPush(redisKey, 101);
        redisTemplate.opsForList().leftPush(redisKey, 102);
        redisTemplate.opsForList().leftPush(redisKey, 103);

        System.out.println(redisTemplate.opsForList().size(redisKey));
        System.out.println(redisTemplate.opsForList().index(redisKey, 0));
        System.out.println(redisTemplate.opsForList().index(redisKey, 2));
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, 2));

        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
        System.out.println(redisTemplate.opsForList().leftPop(redisKey));
    }

    // 测试对set集合的操作
    @Test
    public void testSets() {
        String redisKey = "test:teachers";

        redisTemplate.opsForSet().add(redisKey, "刘备", "关羽", "张飞", "赵云", "诸葛亮");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
        System.out.println(redisTemplate.opsForSet().members(redisKey));
    }

    // 测试redis对有序集合Set的处理
    @Test
    public void testSortedSets() {
        String redisKey = "test:students";

        redisTemplate.opsForZSet().add(redisKey, "唐僧", 80);
        redisTemplate.opsForZSet().add(redisKey, "孙悟空", 90);
        redisTemplate.opsForZSet().add(redisKey, "猪八戒", 70);
        redisTemplate.opsForZSet().add(redisKey, "沙僧", 60);
        redisTemplate.opsForZSet().add(redisKey, "白龙马", 50);

        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "猪八戒"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "猪八戒"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
    }

    // 测试公用方法
    @Test
    public void testKeys() {
        redisTemplate.delete("test:user");

        System.out.println(redisTemplate.hasKey("test:user"));

        redisTemplate.expire("test:students", 10, TimeUnit.SECONDS);
    }

    // 多次访问一个key，可以进行绑定，简化代码
    @Test
    public void testBoundOperations() {
        String redisKey = "test:user";
        BoundHashOperations hashOperations = redisTemplate.boundHashOps(redisKey);
        hashOperations.put("username", "zhang");
        System.out.println(hashOperations.get("username"));
    }

    // 编程式事务
    // 事务统一处理，即先将数据一起打包，一同处理，在中间查询，是查询不到的
    @Test
    public void testTransaction() {
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "test:tx";

                // 事务启动
                redisOperations.multi();

                redisOperations.opsForSet().add(redisKey, "zhangSan");
                redisOperations.opsForSet().add(redisKey, "liShi");
                redisOperations.opsForSet().add(redisKey, "wanWu");

                // 尝试在事件中间查询，是查询不到的
                System.out.println(redisOperations.opsForSet().members(redisKey));

                // 事件开始处理
                return redisOperations.exec();
            }
        });

        System.out.println(redisTemplate.opsForSet().members("test:tx"));
        System.out.println(obj);
    }
}
