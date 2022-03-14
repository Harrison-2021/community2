package com.nowcoder.community;

import com.nowcoder.community.dao.DiscussPostMapper;
import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.MessageMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTest {
    @Autowired
    UserMapper userMapper;
    @Autowired
    DiscussPostMapper discussPostMapper;
    @Autowired
    LoginTicketMapper loginTicketMapper;

    @Autowired
    MessageMapper messageMapper;

//    用户表相关sql语句操作的测试
    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);
        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
        user = userMapper.selectByName("liubei");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeader(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

//    帖子表相关sql语句操作的测试
    @Test
    public void testGetPost() {
        List<DiscussPost> list = discussPostMapper.getPosts(149, 0, 10);
        for(DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.getPostRows(149);
        System.out.println(rows);
    }

// 登录凭证数据的测试
    // 添加数据
    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(199);
        loginTicket.setTicket("test");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10)); // 10分钟后过期
        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    // 查询和修改数据
    @Test
    public void testSelectLoginTicket() {
        LoginTicket ticket = loginTicketMapper.selectByTicket("test");
        System.out.println(ticket);

        loginTicketMapper.updateLoginTicket("test", 1);
        ticket = loginTicketMapper.selectByTicket("test");
        System.out.println(ticket.getStatus());
    }

    // 添加帖子
    @Test
    public void testInsertPosts() {
        DiscussPost discussPost = new DiscussPost();
        discussPost.setContent("你好");
        discussPost.setTitle("测试");
        discussPost.setCreateTime(new Date());
        discussPostMapper.insertPosts(discussPost);
    }

    // 查询帖子
    @Test
    public void testSelectPosts() {
        System.out.println(discussPostMapper.selectPost(283));
    }

    @Test
    public void testSelectLetters() {
        int i = 0;
        List<Message> list = messageMapper.selectConversationList(111, 0, 20);
        for(Message message : list) {
            System.out.println((++i) + " : " + message);
        }

        System.out.println("111会话列表总数 " + messageMapper.selectConversationCount(111));

        list = messageMapper.selectLetterList("111_112", 0, 10);
        i = 0;
        for(Message message : list) {
            System.out.println((++i) + " : " + message);
        }

        System.out.println("111与112会话列表总数 " + messageMapper.selectLetterCount("111_112"));

        System.out.println("111未读私信总数 " + messageMapper.selectUnreadCount(131, null));
        System.out.println("111_131未读私信总数 " + messageMapper.selectUnreadCount(131, "111_131"));

    }
}

