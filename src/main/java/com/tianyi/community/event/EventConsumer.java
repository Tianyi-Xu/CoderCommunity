package com.tianyi.community.event;

import com.alibaba.fastjson.JSONObject;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import com.tianyi.community.entity.DiscussPost;
import com.tianyi.community.entity.Event;
import com.tianyi.community.entity.Message;
import com.tianyi.community.service.DiscussPostService;
import com.tianyi.community.service.ElasticsearchService;
import com.tianyi.community.service.MessageService;
import com.tianyi.community.util.CommunityConstant;
import com.tianyi.community.util.CommunityUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.config.ElasticsearchNamespaceHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("qiniu.bucket.share.name")
    private String shareBucketName;
    
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("Message is empty");
            return;
        }

        Event event  = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("The format of message is not complied");
            return;
        }

        // system message
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        // content to render on page
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("eventType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        // put all the other data to the content
        if (event.getOtherData() != null || !event.getOtherData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getOtherData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("Message is empty");
            return;
        }

        Event event  = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("The format of message is not complied");
            return;
        }

        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("Message is empty");
            return;
        }

        Event event  = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("The format of message is not complied");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            logger.error("Message is empty");
            return;
        }

        Event event  = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            logger.error("The format of message is not complied");
            return;
        }

        // generate the web page image
        String htmlUrl = (String) event.getOtherData().get("htmlUrl");
        String fileName = (String) event.getOtherData().get("fileName");
        String suffix = (String) event.getOtherData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 "
                + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try {
            // a new process, independent of current application, take long time to generate the image
            // main thread continue, doesn't wait for the file to be completed
            Process p = Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功: " + cmd);
        } catch (IOException e) {
            logger.error("生成长图失败: " + e.getMessage());
        }

        // use a scheduler to check if the file has been successfully generated, then upload to bucket
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500); // 0.5 sec
        task.setFuture(future);



    }

    class UploadTask implements Runnable {

        private String fileName;
        private String suffix;
        // task status, can be used to cancel the scheduler
        private Future future;
        // start time of the task
        private long startTime;
        // retried times
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future) {
            this.future = future;
        }

        @Override
        public void run() {
            // too long since the start time (30 secs)
            if (System.currentTimeMillis() - startTime > 30000) {
                logger.error("Task being processed for too long since the start time, cancel the upload: " + fileName);
                future.cancel(true);
                return;
            }
            // uploaded for too many times
            if (uploadTimes >= 5) {
                logger.error("upload too many times, cancel the upload: " + fileName);
                // cancel schedule
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);

            // when image has generated,  upload the photo to Cloud bucket
            if (file.exists()) {
                logger.info(String.format("Start uploading for the %d time [%s]:", ++uploadTimes, fileName));

                // expected return info from qiniu if uploaded
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));

                // generate qiniu upload token, expire in 1h
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);

                // set upload region to US
                UploadManager manager = new UploadManager(new Configuration(Zone.zoneNa0()));

                try {
                    // upload
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);

                    // parse response
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0")) {
                        logger.info(String.format("uploaded failed for %d times: [%s].", uploadTimes, fileName));
                    } else {
                        logger.info(String.format("uploaded successfully at the %d times: [%s].", uploadTimes, fileName));
                        // uploaded, cancel future retries
                        future.cancel(true);
                    }
                } catch (QiniuException e) {
                    logger.info(e.getMessage());
                    logger.info(String.format("uploaded failed for %d times: [%s].", uploadTimes, fileName));
                }
            } else {
                logger.info("waiting for the share image to be generated [" + fileName + "].");
            }
        }
    }


}
