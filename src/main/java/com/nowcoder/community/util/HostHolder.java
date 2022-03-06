package com.nowcoder.community.util;

import com.nowcoder.community.entity.User;
import org.springframework.stereotype.Component;

@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();

    // 往单个个线程都存user信息
    // 先拿到当前线程，再往当前线程里存值
    public void setUsers(User user) {
        users.set(user);
    }

    // 先拿到当前线程，再从当前线程里取值
    public User getUser() {
        return users.get();
    }

    // 同理，清理当前线程的user值
    public void clear() {
        users.remove();
    }
}
