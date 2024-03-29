package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.MailClient;
import com.nowcoder.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    //private LoginTicketMapper loginTicketMapper;
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPth;

    /**
     * 根据id查询用户-优先从缓存中查，缓存没有，就初始化缓存，再拿到用户信息
     * @param userId
     * @return
     */
    public User findUserById(int userId) {
    //return userMapper.selectById(userId);
        User user = getUserCache(userId);
        if(user == null) {
            user = initUserCache(userId);
        }
        return user;
    }

    /**
     * 根据用户名查询用户
     * @param name
     * @return
     */
    public User findUserByName(String name) {
        return userMapper.selectByName(name);
    }

    /**
     * 注册业务
     * @param user，用户信息
     * @return  返回的注册错误信息
     */
    public Map<String, Object> register(User user) {
        Map<String, Object> falseMap = new HashMap<>();
//        1.验证用户输入内容是否有问题
        // 空值处理
        // 用户对象不能为null
        if(user == null) {
            throw new IllegalArgumentException("注册用户不能为空");
        }

        // 内容不能为空
        // 用户名
        if(StringUtils.isBlank(user.getUsername())) {
            falseMap.put("usernameMsg", "用户名不能为空!");
            return falseMap;
        }
        // 密码
        if(StringUtils.isBlank(user.getPassword())) {
            falseMap.put("passwordMsg", "密码不能为空!");
            return falseMap;
        }
        // 邮箱
        if(StringUtils.isBlank(user.getEmail())) {
            falseMap.put("emailMsg", "邮箱不能为空!");
            return falseMap;
        }
        // 与数据库比对验证
        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if(u != null) {
            falseMap.put("usernameMsg", "该账号已存在!"); // msg信息覆盖
            return falseMap;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if(u != null) {
            falseMap.put("emailMsg", "该邮箱已存在!");
            return falseMap;
        }


//      2. 验证无误，将用户信息入库，并向用户发送激活链接
        // 先将密码进行MD5加密
        String salt = CommunityUtil.generateUUID().substring(0, 5);
        user.setSalt(salt);
        user.setPassword(CommunityUtil.md5(user.getPassword() + salt));
        // 其他状态设置
        user.setType(0); // 普通用户
        user.setStatus(0); // 未激活
        String code = CommunityUtil.generateUUID();
        user.setActivationCode(code); // 激活码
        user.setCreateTime(new Date()); // 创建时间
        user.setHeaderUrl(String.format(    // 用户头像，随机生成
                "http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        // 入库
        userMapper.insertUser(user);

//      3. 向注册用户邮箱发邮件
        // 创建封装数据的Context,封装数据
        Context context = new Context();    // thymeleaf模板中的Context,相当于model
        context.setVariable("toMsg", user.getEmail());
        // 拼接激活链接url，http://localhost:8080/community/activation/101/code
        String url = domain + contextPth + "/activate" + "/" + user.getId() + "/" + code;
        context.setVariable("urlMsg", url);

        // 发送HTML邮件
        // 利用模板生成动态网页，需将模板视图传过去
        // 模板引擎自动识别context传入的参数，然后动态加载到网页中，
        // 将网页的动态变量进行替换，并将网页内容加载到content中
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "交流网用户注册激活链接", content);
        return falseMap;
    }

    /**
     * 处理用户激活业务
     * @param userId 解析url中的userID,定位指定用户
     * @param code  解析url中的激活码
     * @return      返回激活状态，用静态变量展示
     */
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {   // 重复激活
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) { // 激活码匹配成功
            // 更新用户状态，激活成功-同时删除缓存-要先更新，再删除，防止更新不成功
            // 注意缓存不一致的问题
            userMapper.updateStatus(userId, 1);
            clearUserCache(userId);
            return ACTIVATION_SUCCESS;
        } else {    // 激活码不匹配，不能激活
            return ACTIVATION_FAILURE;
        }
    }

    /**
     * 登录业务处理
     * @param username
     * @param password
     * @param expiredSeconds 过期时间
     * @return map
     */
    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();
        // 判断空值
        if(StringUtils.isBlank(username)) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if(StringUtils.isBlank(password)) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }

        // 验证合法性
        // 验证账号
        User user = userMapper.selectByName(username);
        if(user == null) {
            map.put("usernameMsg", "账号不存在!");
            return map;
        }
        if(user.getStatus() == 0) {
            map.put("usernameMsg", "账号没有激活!");
            return map;
        }
        // 验证密码
        String salt = user.getSalt();
        if(!user.getPassword().equals(CommunityUtil.md5(password + salt))) {
            map.put("passwordMsg", "密码不正确!");
            return map;
        }

        // 登录前创建登录凭证，并在服务端和客户端储存
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0); // 0有效，1无效，登出时设置为1
        // 注意日期转换的格式单位：毫秒
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000L));
        //loginTicketMapper.insertLoginTicket(loginTicket);
        // 将登录凭证储存到redis中，
        // 注意，redis的数据不设定过期时间，这个数据长久保存，今后扩展功能有用
        // value储存对象-JSON字符串
        String ticketKey = RedisKeyUtil.getTicket(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey, loginTicket);

        // 将登录凭证ticket字符串传给Controller
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    /** 登出业务处理*/
    public void logout(String ticket) {
    //loginTicketMapper.updateLoginTicket(ticket, 1);
        // 由于value储存的是一个对象，要改变对象里面的属性，需要先取出来，改变后，再存回去
        String ticketKey = RedisKeyUtil.getTicket(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey, loginTicket);
    }

    /**
     * 根据用户凭证ticket查询t票类
     * @param ticket    用户cookie中储存的ticket字符串
     * @return
     */
    public LoginTicket selectByTicket(String ticket) {
        //return loginTicketMapper.selectByTicket(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(RedisKeyUtil.getTicket(ticket));
    }

    /**
     *  更改用户头像
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId, String headerUrl) {
        int row = userMapper.updateHeader(userId, headerUrl);
        clearUserCache(userId);
        return row;
    }

    /**
     * 1.查询用户时，先从缓存中取值-若取不到，进行第二步初始化缓存数据
     * @param userId    要查询用户的id
     * @return          查询到的用户
     */
    public User getUserCache(int userId) {
        String userKey = RedisKeyUtil.getUser(userId);
        return (User) redisTemplate.opsForValue().get(userKey);
    }

    /**
     * 2.初始化缓存-将mysql中查询到的用户缓存到redis中，并设置过期时间
     * @param userId
     * @return
     */
    public User initUserCache(int userId) {
        // 从mysql数据中查询到用户，进行缓存
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtil.getUser(userId);
        redisTemplate.opsForValue().set(userKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 3.清理缓存-当user数据变更时，直接清除缓存，而不是修改，这样防止并发情况
     * @param userId
     */
    public void clearUserCache(int userId) {
        String userKey = RedisKeyUtil.getUser(userId);
        redisTemplate.delete(userKey);
    }
}
