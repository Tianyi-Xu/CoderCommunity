package com.tianyi.community;

import com.tianyi.community.dao.DiscussPostMapper;
import com.tianyi.community.dao.UserMapper;
import com.tianyi.community.entity.DiscussPost;
import com.tianyi.community.entity.User;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        user = userMapper.selectByName("liubei");
        System.out.println(user);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abs");
        user.setEmail("test@sina.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateStatus() {
        User user = userMapper.selectById(150);

        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);
        System.out.println(user.getStatus());

        rows = userMapper.updateHeaderUrl(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);
        System.out.println(user.getHeaderUrl());;

        rows = userMapper.updatePassword(150, "222222");
        System.out.println(rows);
        System.out.println(user.getPassword());
    }

    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(103,0, 10);
        for (DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(103);
        System.out.println("******" + rows);
    }





}
