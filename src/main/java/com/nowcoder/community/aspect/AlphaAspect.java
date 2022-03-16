package com.nowcoder.community.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
//
//@Component
//@Aspect
public class AlphaAspect {
    /**
     * 声明切点-并定义织入位置
     * 所有返回类型
     * 所有service包下所有Service业务组件
     * 所有业务组件下的所有方法
     * 所有方法中的所有参数
     */
    @Pointcut("execution(* com.nowcoder.community.service.*.*(..))")
    public void pointCut() {}

    /**
     * 连接点之前织入
     */
    @Before("pointCut()")
    public void before() {
        System.out.println("before");
    }

    /**
     * 连接点之后织入
     */
    @After("pointCut()")
    public void after() {
        System.out.println("after");
    }

    /**
     * 方法返回后织入
     */
    @AfterReturning("pointCut()")
    public void afterReturning() {
        System.out.println("afterReturning");
    }

    /**
     * 方法出现异常时织入
     */
    @AfterThrowing("pointCut()")
    public void afterThrowing() {
        System.out.println("afterThrowing");
    }

    /**
     * 连接点前后均织入
     * @param joinPoint  原始目标组件的连接点
     * @return           原始目标组件连接点中执行方法后的返回值
     * @throws Throwable
     */
    @Around("pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("around before");
        Object obj = joinPoint.proceed();
        System.out.println("around after");
        return obj;
    }
}
