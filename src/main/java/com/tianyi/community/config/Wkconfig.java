package com.tianyi.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class Wkconfig {

    private static final Logger logger = LoggerFactory.getLogger(Wkconfig.class);
    @Value("${wk.image.storage}")
    private String wkImageStorage;


    @PostConstruct
    public void init() {
        // create the image folder if not exists when start the application
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdir();
            logger.info("created WK image folder" + wkImageStorage);
        }

    }
}
