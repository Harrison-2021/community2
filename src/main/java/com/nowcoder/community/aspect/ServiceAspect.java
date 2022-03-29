package com.nowcoder.community.aspect;

import org.apache.catalina.Server;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 定义切点
     */
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointCut() {}

    /**
     * 在连接点之前-处理业务组件之前记录日志
     * @param joinPoint
     */
    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        // 用户[1.2.3.4],在[xxx],访问了[com.nowcoder.community.service.xxx()].
        // 获取用户信息
        // 获取请求上下文
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(requestAttributes == null) { // 不是通过controller调用service
            return;
        }
        // 获取请求
        HttpServletRequest request = requestAttributes.getRequest();
        // 用户ip
        String ip = request.getRemoteHost();
        // 当前时间
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        // 连接点织入的业务组件子类代理(原始方法)信息-类全路径名-方法名
        String target = joinPoint.getSignature().getDeclaringTypeName() +
                "." + joinPoint.getSignature().getName();
//        String target = joinPoint.getSignature().toString();
        // 记录日志
        logger.info(String.format("用户[%s], 在[%s], 访问了[%s].", ip, now, target));
    }
}
