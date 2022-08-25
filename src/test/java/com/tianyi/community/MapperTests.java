package com.tianyi.community;

import com.tianyi.community.dao.*;
import com.tianyi.community.entity.*;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MessageMapper messageMapper;

    /**
     * test for UserMapper
     */
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

    /**
     * test for DiscussionPostMapper
     */
    @Test
    public void testSelectPosts() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(103,0, 10, 0);
        for (DiscussPost post : list) {
            System.out.println(post);
        }

        int rows = discussPostMapper.selectDiscussPostRows(103);
        System.out.println("******" + rows);
    }

    /**
     * test for LoginTicketMapper
     */
    @Test
    public void testInsertLoginTicket() {
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abc");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10)); // 10 mins

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket loginTicket = loginTicketMapper.selectByTicket("abc");
        System.out.println(loginTicket);

        loginTicketMapper.updateStatus("abc",1);
        System.out.println(loginTicket);
    }

    @Test
    public void selectMessages() {
        List<Message> list = messageMapper.selectConversationsByUserId(111, 0, 20);
        for (Message message : list) {
            System.out.println(message);
        }

        int count = messageMapper.selectConversationCount(111);
        System.out.println(count);

        list = messageMapper.selectMessagesByConversation("111_112", 0, 10);
        System.out.println(list);

        count = messageMapper.selectUnreadMessageCountByUserId(131, "111_131");
        System.out.println(count);
    }




}
