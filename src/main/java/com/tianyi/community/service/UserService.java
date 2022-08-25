package com.tianyi.community.service;

import com.tianyi.community.dao.LoginTicketMapper;
import com.tianyi.community.dao.UserMapper;
import com.tianyi.community.entity.LoginTicket;
import com.tianyi.community.entity.User;
import com.tianyi.community.util.CommunityConstant;
import com.tianyi.community.util.CommunityUtil;
import com.tianyi.community.util.MailClient;
import com.tianyi.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class

UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        if (user == null) {
            throw new IllegalArgumentException("Argument can't be null");
        }

        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "username can't be empty");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "password can't be empty");
            return map;
        }

        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "email can't be empty");
            return map;
        }

        // check if user already exists
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "The username has already be registered.");
            return map;
        }

        // check if email already be registered with
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "The email has already be registered.");
            return map;
        }

        // register user
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("https://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user); // mybatis generate id automatically

        // send user activation email
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        //http://localhost:8080/community/activation/{userId}/{code}
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String mailContent = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "Activate your bearcoder account", mailContent);

        return map;
    }

    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ACTIVATION_FAILURE;
        }

        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }

    public Map<String, Object> login(String username, String password, int expiredSeconds) {
        Map<String, Object> map = new HashMap<>();

        // check null values
        if (StringUtils.isBlank(username)) {
            map.put("usernameMsg", "username can't be blank");
            return map;
        }

        if (StringUtils.isBlank(password)) {
            map.put("passwordMsg", "password can't be blank");
            return map;
        }

        // verify the account
        User user = userMapper.selectByName(username);
        if (user == null) {
            map.put("usernameMsg", "username doesn't exist");
            return map;
        }

        if (user.getStatus() == 0) {
            map.put("usernameMsg", "user account is not activated");
            return map;
        }

        password = CommunityUtil.md5(password + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("passwordMsg", "password is wrong");
            return map;
        }

        // generate loginTicket
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));

        // store loginTicket in Redis
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey, loginTicket); // redis will serialize the login ticket


        // return ticket to the client
        map.put("ticket", loginTicket.getTicket());
        return map;
    }

    public void logout(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        // redis deserialize to login ticket
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        // after change, store the ticket again
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    public LoginTicket findLoginTicket(String ticket) {
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    public int updateProfilePhoto(int userId, String photoUrl) {
        int rows = userMapper.updateHeaderUrl(userId, photoUrl);
        clearCache(userId);
        return rows;
    }

    public Map<String, Object> updatePassword(int userId, String currentPassword, String newPassword) {
        Map<String, Object> map = new HashMap<>();

        if (StringUtils.isBlank(currentPassword)) {
            map.put("currentPasswordMsg", "current password can't be blank");
            return map;
        }

        if (StringUtils.isBlank(newPassword)) {
            map.put("newPasswordMsg", "new password can't be blank");
            return map;
        }

        // verify user current password
        User user = userMapper.selectById(userId);
        String password = CommunityUtil.md5(currentPassword + user.getSalt());
        if (!user.getPassword().equals(password)) {
            map.put("currentPasswordMsg", "password is wrong. Please try again");
            return map;
        }

        if (currentPassword.equals(newPassword)) {
            map.put("newPasswordMsg", "new password can't be the same as current password");
            return map;
        }
        newPassword = CommunityUtil.md5(newPassword + user.getSalt());
        userMapper.updatePassword(userId, newPassword);
        clearCache(userId);
        return map;
    }

    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }

    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // if we can't get user from cache, put user in cache
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }


}
