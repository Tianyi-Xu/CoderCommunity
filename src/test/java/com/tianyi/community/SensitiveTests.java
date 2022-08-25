package com.tianyi.community;

import com.tianyi.community.util.WordFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTests {

    @Autowired
    private WordFilter wordFilter;

    @Test
    public void testSensitiveFilter() {
        String text = "this is a place can do drug and fuck you";
        text = wordFilter.filter(text);
        System.out.println(text);
        text = "this is a place can do $d$ru$g and fuck you";
        text = wordFilter.filter(text);
        System.out.println(text);
    }



}
