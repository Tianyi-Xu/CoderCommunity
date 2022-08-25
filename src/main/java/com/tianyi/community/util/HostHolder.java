package com.tianyi.community.util;

import com.tianyi.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * Wrapper class for ThreadLocal,
 * An alternative way to hold user information, as a substitute to Session
 */
@Component
public class HostHolder {

    private ThreadLocal<User> userThreadLocal = new ThreadLocal<>();

    public void setUser(User user) {
        userThreadLocal.set(user);
    }

    public User getUser() {
        return userThreadLocal.get();
    }

    public void clear() {
        userThreadLocal.remove();
    }

}
