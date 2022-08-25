package com.tianyi.community;

import com.tianyi.community.entity.DiscussPost;
import com.tianyi.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService postService;

//    @Test
//    public void initDataForTest() {
//        for (int i = 0; i < 300000; i++) {
//            DiscussPost post = new DiscussPost();
//            post.setUserId(111);
//            post.setTitle("Does anyone know how to solve leetcode " + i);
//            post.setContent("DM me to discuss or feel free to comment below!");
//            post.setCreateTime(new Date());
//            post.setScore(Math.random() * 2000);
//            postService.addDiscussPost(post);
//        }
//    }



    @Test
    public void testCache() {
        System.out.println(postService.findDiscussPosts(0, 30, 40, 1));
        System.out.println(postService.findDiscussPosts(0, 30, 40, 1));
        System.out.println(postService.findDiscussPosts(0, 30, 40, 1));
        System.out.println(postService.findDiscussPosts(0, 0, 10, 0));
    }

}
