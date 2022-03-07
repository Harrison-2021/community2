package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.controller.annotation.LoginRequired;
import com.nowcoder.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    HostHolder hostHolder;

    // 只需在Controller处理请求前拦截即可，一旦拦截，就不会进行下去
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求中的方法，排除不是方法的请求，即静态资源等
        if(handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            Method method = handlerMethod.getMethod(); // 获取请求中的方法
            LoginRequired loginRequired = method.getAnnotation(LoginRequired.class); // 获取方法中的指定类型的注解
            if(loginRequired != null && hostHolder.getUser() == null) { // 方法上有注解且用户没登录
                // 重定向到登录页面，且当前请求停止
                response.sendRedirect(request.getContextPath() + "/login");
                return false;
            }
        }

        return true;
    }
}
