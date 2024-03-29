package com.tianyi.community;

import com.tianyi.community.util.MailClient;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTests {

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine; // used to get thymeleaf templates

    @Test
    public void testTextMail() {
        mailClient.sendMail("xtyskye@yahoo.com", "Test", "Welcome.");
    }

    @Test
    public void testHtmlMail() {
        Context context = new Context();
        context.setVariable("username", "sunday"); // set the variable key-value send to thymeleaf template
        String content = templateEngine.process("/mail/demo", context);
        mailClient.sendMail("xtyskye@yahoo.com", "Test", content);
    }


}
