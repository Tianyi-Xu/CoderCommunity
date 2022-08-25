package com.tianyi.community.controller.interceptor;

import com.tianyi.community.entity.User;
import com.tianyi.community.service.StatisticsService;
import com.tianyi.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class StatisticsInterceptor implements HandlerInterceptor {

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private HostHolder hostHolder;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // UV
        String ip = request.getRemoteHost();
        statisticsService.recordUV(ip);

        // DAU
        User user = hostHolder.getUser();
        if (user != null) {
            statisticsService.recordDAU(user.getId());
        }
        return true;
    }

}
