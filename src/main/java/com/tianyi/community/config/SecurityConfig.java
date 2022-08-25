package com.tianyi.community.config;

import com.tianyi.community.util.CommunityConstant;
import com.tianyi.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;


@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // Authorization
        // can only visit those after authenticated(log in)
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "user/upload",
                        "discuss/add",
                        "/comment/add/**",
                        "/message/**",
                        "notice/**",
                        "like",
                        "/follow",
                        "unfollow"
                ).hasAnyAuthority(
                    AUTHORITY_USER, AUTHORITY_MODERATOR, AUTHORITY_ADMIN
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/hot"
                )
                .hasAuthority(AUTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/statistics/**",
                        "/actuator/**"
                )
                .hasAuthority(AUTHORITY_ADMIN)
                .anyRequest().permitAll()
                .and().csrf().disable();

        // when user doesn't have the required authority
        http.exceptionHandling()
                .authenticationEntryPoint(
                        new AuthenticationEntryPoint() {
                            // not authenticated(not logged in)
                            @Override
                            public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                                // for normal request, redirect to the login page
                                // for async request, return JSON
                                String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                                if ("XMLHttpRequest".equals(xRequestedWith)) {
                                    httpServletResponse.setContentType("application/plain;charset=utf-8");
                                    PrintWriter writer = httpServletResponse.getWriter();
                                    writer.write(CommunityUtil.getJSONString(403, "Please log in."));
                                } else {
                                    httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/login");
                                }
                            }
                        }
                )
                .accessDeniedHandler(
                        new AccessDeniedHandler() {
                            // not have the required authority
                            @Override
                            public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AccessDeniedException e) throws IOException, ServletException {
                                String xRequestedWith = httpServletRequest.getHeader("x-requested-with");
                                if ("XMLHttpRequest".equals(xRequestedWith)) {
                                    httpServletResponse.setContentType("application/plain;charset=utf-8");
                                    PrintWriter writer = httpServletResponse.getWriter();
                                    writer.write(CommunityUtil.getJSONString(403, "You don't have the authorization."));
                                } else {
                                    httpServletResponse.sendRedirect(httpServletRequest.getContextPath() + "/denied");
                                }
                            }
                        }
                );

        // deactivate spring security's filtering logout
        http.logout().logoutUrl("/securitylogout");
    }



}
