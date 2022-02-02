package com.nowcoder.community;

import com.nowcoder.community.dao.AlphaDAO;
import com.nowcoder.community.service.AlphaService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CommunityApplicationTests implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Test
	public void testApplicationContext() {
		// 测试容器是否添加成功
		System.out.println(applicationContext);

		// 测试通过容器获得bean，只通过类名和注解
		AlphaDAO alphaDAO = applicationContext.getBean(AlphaDAO.class);
		System.out.println(alphaDAO.select());

		// 测试通过容器获得bean，还通过bean的名字，类名和注解
		alphaDAO = applicationContext.getBean("Hibernate", AlphaDAO.class);
		System.out.println(alphaDAO.select());
	}

	// 测试对Bean的初始化和销毁的管理
	@Test
	public void testBeanManagement() {
		AlphaService alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);

//		默认只创建和销毁对象一次
		alphaService = applicationContext.getBean(AlphaService.class);
		System.out.println(alphaService);
	}

	// 测试对标准库中的类的Bean管理
	@Test
	public void testBeanConfig() {
		SimpleDateFormat simpleDateFormat = applicationContext.getBean(SimpleDateFormat.class); //
		System.out.println(simpleDateFormat.format(new Date()));
	}

	// 直接自动依赖注入Bean
	@Autowired
	@Qualifier("Hibernate")
	private AlphaDAO alphaDAO;

	@Test
	public void testDI() {
		System.out.println(alphaDAO.select());
	}
}
