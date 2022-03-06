package com.nowcoder.community.controller.interceptor;

import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CookieUtil;
import com.nowcoder.community.util.HostHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {
    private Logger logger = LoggerFactory.getLogger(LoginTicketInterceptor.class);
    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    // 在Controller之前执行
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug("preHandle " + handler.toString());
        // 1.获取ticket
        String ticket = CookieUtil.getValue(request, "ticket");
        // 2.根据ticket查询user
        if(ticket != null) {
            // 向数据库中查询凭证
            LoginTicket loginTicket = userService.selectByTicket(ticket);
            if(loginTicket != null &&
                loginTicket.getStatus() == 0 &&
                loginTicket.getExpired().after(new Date())) {
                // 有效，就根据凭证找到用户信息
                User user = userService.findUserById(loginTicket.getUserId());
                // 在本次请求中持有用户信息，在请求结束前一直保存在请求的当前线程容器中
                hostHolder.setUsers(user);
            }
        }
        return true;    // true，表示拦截处理之后继续执行
    }

    // 在Controller之后，模板视图渲染前执行
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 在渲染前获取用户信息具体，进行渲染
        User user = hostHolder.getUser();
        if(user != null && modelAndView != null) {
            logger.debug("postHandle " + user.toString());
            modelAndView.addObject("loginUser", user);
        }
    }

    // 在模板视图渲染后处理，一般做清理工作
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        logger.debug("afterHandle " + handler.toString());
        hostHolder.clear();
    }
}
