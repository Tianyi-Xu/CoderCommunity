package com.tianyi.community.quartz;

import com.tianyi.community.entity.DiscussPost;
import com.tianyi.community.service.DiscussPostService;
import com.tianyi.community.service.ElasticsearchService;
import com.tianyi.community.service.LikeService;
import com.tianyi.community.util.CommunityConstant;
import com.tianyi.community.util.CommunityUtil;
import com.tianyi.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PostScoreRefreshJob implements Job, CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    private static final Date start;

    static {
        Date startTemp;
        try {
            startTemp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-04-01 00:00:00");
        } catch (ParseException e) {
            startTemp = null;
            throw new RuntimeException("initialize start time failed", e);
        }
        start = startTemp;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        // early return if no data stored in the key
        if (operations.size() == 0) {
            logger.info("[cancel the job] no post score need to be recalculated");
            return;
        }

        logger.info("[job started] calculating post scores ..., post size: " + operations.size());
        while(operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[job done] Refresh post scores done.");
    }

    private void refresh(int postId) {
        DiscussPost post = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            logger.error("post doesn't exist: " + postId);
        }

        // if post is useful
        boolean useful = post.getStatus() == 1;
        // comment count
        int  commentCount = post.getCommentCount();
        // like count
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // calculate the weight
        double w = (useful? 75 : 0) + commentCount * 10 + likeCount * 2;
        // calculate the hot score
        double score = Math.log10(Math.max(w, 1)) + ((post.getCreateTime().getTime() - start.getTime())/1000 * 3600 *24);
        // update the hot score
        discussPostService.updateScore(postId, score);
        // modify the post in elasticsearch
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);

    }
}
