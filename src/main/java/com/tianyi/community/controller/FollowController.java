package com.tianyi.community.controller;

import com.tianyi.community.annotation.LoginRequired;
import com.tianyi.community.entity.Event;
import com.tianyi.community.entity.Page;
import com.tianyi.community.entity.User;
import com.tianyi.community.event.EventProducer;
import com.tianyi.community.service.FollowService;
import com.tianyi.community.service.UserService;
import com.tianyi.community.util.CommunityConstant;
import com.tianyi.community.util.CommunityUtil;
import com.tianyi.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements CommunityConstant {

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired
    @RequestMapping(path="/follow", method = RequestMethod.POST)
    @ResponseBody
    public String follow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.follow(user.getId(), entityType, entityId);

        // send system notice about follow
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setEntityType(entityType) // follow user
                .setEntityId(entityId) // user id
                .setEntityUserId(entityId); // still user id
        eventProducer.fireEvent(event);

        return CommunityUtil.getJSONString(0, "Successfully followed.");
    }

    @LoginRequired
    @RequestMapping(path="/unfollow", method = RequestMethod.POST)
    @ResponseBody
    public String unfollow(int entityType, int entityId) {
        User user = hostHolder.getUser();
        followService.unfollow(user.getId(), entityType, entityId);

        return CommunityUtil.getJSONString(0, "Successfully unfollowed.");
    }

    @RequestMapping(path="/followees/{userId}", method = RequestMethod.GET)
    public String getFollowees(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("User doesn't exist.");
        }

        // the user for the followee page
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followees/" + userId);
        page.setRows((int) followService.findFolloweeCount(userId, ENTITY_TYPE_USER));

        List<Map<String, Object>> userList = followService.findFollowees(userId, page.getOffSet(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map: userList) {
                User followee = (User) map.get("user");
                //  the current login user has followed this followee
                map.put("hasFollowed", hasFollowed(followee.getId()));
            }
        }
        model.addAttribute("followees", userList);
        return "/site/followee";
    }

    @RequestMapping(path="/followers/{userId}", method = RequestMethod.GET)
    public String getFollowers(@PathVariable("userId") int userId, Page page, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("User doesn't exist.");
        }

        // the user for the followee page
        model.addAttribute("user", user);

        page.setLimit(5);
        page.setPath("/followers/" + userId);
        page.setRows((int) followService.findFollowerCount(ENTITY_TYPE_USER, userId));

        List<Map<String, Object>> userList = followService.findFollowers(userId, page.getOffSet(), page.getLimit());
        if (userList != null) {
            for (Map<String, Object> map: userList) {
                User followee = (User) map.get("user");
                //  the current login user has followed this followee
                map.put("hasFollowed", hasFollowed(followee.getId()));
            }
        }
        model.addAttribute("followers", userList);
        return "/site/follower";
    }

   // check if current user has followed the given user
    private boolean hasFollowed(int userId) {
        if (hostHolder.getUser() == null) {
            return false;
        }

        return followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
    }



}
