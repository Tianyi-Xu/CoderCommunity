package com.tianyi.community;

import com.tianyi.community.dao.DiscussPostMapper;
import com.tianyi.community.dao.UserMapper;
import com.tianyi.community.entity.DiscussPost;
import com.tianyi.community.entity.User;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class LoggerTests {

    private static final Logger logger = LoggerFactory.getLogger(LoggerTests.class); // classname will be the logger name

    @Test
    public void testLogger() {
        System.out.println(logger.getName());
        logger.trace("trace log");
        logger.debug("debug log"); // 正常调试程序时
        logger.info("info log");
        logger.warn("warn log");
        logger.error("error log");

    }





}

