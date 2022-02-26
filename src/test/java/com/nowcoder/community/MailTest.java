package com.nowcoder.community;

import com.nowcoder.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {
    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Test
    public void testTestMail() {
        mailClient.sendMail("liushenggcs@163.com", "TEST", "welcome");
    }

    @Test
    public void testHtmlMail() {
        // 创建封装数据的model
        Context context = new Context();
        context.setVariable("username", "sunday");

        //利用模板生成动态网页
        String content = templateEngine.process("/mail/demo", context);
        System.out.println(content);

        //用框架发发邮件，内容是模板引擎获取的内容
        mailClient.sendMail("liushenggcs@163.com", "HTML", content);
    }
}
