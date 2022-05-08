package com.tianyi.community.controller;

import com.tianyi.community.entity.DiscussPost;
import com.tianyi.community.entity.Page;
import com.tianyi.community.entity.User;
import com.tianyi.community.service.DiscussPostService;
import com.tianyi.community.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {
    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @RequestMapping(path="/index", method= RequestMethod.GET)
    public String getIndexPage(Model model, Page page) {
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0, page.getOffSet(), page.getLimit());
        List<Map<String, Object>> discussPosts = new ArrayList<>();

        for (DiscussPost post : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("post", post);
            User user = userService.findUserById(post.getUserId());
            map.put("user", user);
            discussPosts.add(map);
        }

        model.addAttribute("discussPosts", discussPosts);
        return "/index";
    }





}
