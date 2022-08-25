package com.tianyi.community.controller;


import com.tianyi.community.entity.*;
import com.tianyi.community.event.EventProducer;
import com.tianyi.community.service.CommentService;
import com.tianyi.community.service.DiscussPostService;
import com.tianyi.community.service.LikeService;
import com.tianyi.community.service.UserService;
import com.tianyi.community.util.CommunityConstant;
import com.tianyi.community.util.CommunityUtil;
import com.tianyi.community.util.HostHolder;
import com.tianyi.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path="/add", method= RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "Need to login first to add post.");
        }

        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // Produce kafka event to save the post to elasticsearch
        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(post.getId());
        eventProducer.fireEvent(event);

        // put the post to redis to calculate the hot value later
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, post.getId());


        return CommunityUtil.getJSONString(0, "Successfully published a  post.");
    }

    @RequestMapping(path="/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // find the post
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);

        // find the owner of the post
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // find the like count of the post
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeCount", likeCount);

        // find the like status of the post by the user
        int likeStatus = hostHolder.getUser() == null ?
                0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, discussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // page division
        page.setLimit(5); // every page shows 5 comments
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount()); // set the number of total comments
        model.addAttribute("offset",page.getOffSet());

        // find the comments to the post
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffSet(), page.getLimit());
        // comment and commenter object list
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for(Comment comment : commentList) {
                Map<String, Object> commentVo = new HashMap<>();
                // comment
                commentVo.put("comment", comment);

                // user
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                // find the like count of the comment
                likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                // find the like status of the comment by the user
                likeStatus = hostHolder.getUser() == null ?
                        0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                // the comments to the comment
                List<Comment> replyList =  commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // the recursive comment object list
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyVoList != null) {
                    for (Comment reply: replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // comment target
                        User target = reply.getTargetId() == 0? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);
                        // find the like count of the post
                        likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);
                        // find the like status of the post by the user
                        likeStatus = hostHolder.getUser() == null ?
                                0 : likeService.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);

                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replies", replyVoList);

                // get count of comment
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }

    // set to top
    @RequestMapping(path = "/top", method = RequestMethod.POST)
    @ResponseBody
    public String setTop(int id) {
        User user = hostHolder.getUser();
        discussPostService.updateType(id, 1);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);
        return CommunityUtil.getJSONString(0);
    }

    // set to useful
    @RequestMapping(path = "/useful", method = RequestMethod.POST)
    @ResponseBody
    public String setUseful(int id) {
        User user = hostHolder.getUser();
        discussPostService.updateStatus(id, 1);

        Event event = new Event()
                .setTopic(TOPIC_PUBLISH)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        // put the post to redis to calculate the hot value later
        String redisKey = RedisKeyUtil.getPostScoreKey();
        redisTemplate.opsForSet().add(redisKey, id);

        return CommunityUtil.getJSONString(0);
    }

    @RequestMapping(path = "/delete", method = RequestMethod.POST)
    @ResponseBody
    public String delete(int id) {
        User user = hostHolder.getUser();
        discussPostService.updateStatus(id, 2);

        Event event = new Event()
                .setTopic(TOPIC_DELETE)
                .setUserId(user.getId())
                .setEntityType(ENTITY_TYPE_POST)
                .setEntityId(id);
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0);
    }


}
