package com.tianyi.community.controller;

import com.tianyi.community.entity.DiscussPost;
import com.tianyi.community.entity.Page;
import com.tianyi.community.service.ElasticsearchService;
import com.tianyi.community.service.LikeService;
import com.tianyi.community.service.UserService;
import com.tianyi.community.util.CommunityConstant;
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
public class SearchController implements CommunityConstant {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    // search?keyword=xxx
    @RequestMapping(path="/search", method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model) {
        // search post, note that ES page starts from 0
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                elasticsearchService.searchDiscussPost(keyword, page.getCurrent() - 1, page.getLimit());

        // gather information to return
        List<Map<String, Object>> discussPosts = new ArrayList<>();
        if (searchResult != null) {
            for (DiscussPost post : searchResult) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                map.put("user", userService.findUserById(post.getUserId()));
                map.put("likeCount", likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));
                discussPosts.add(map);
            }
        }
        model.addAttribute("discussPosts", discussPosts);
        model.addAttribute("keyword", keyword);

        // pagination
        page.setPath("/search?keyword=" +keyword);
        page.setRows(searchResult == null? 0 : (int) searchResult.getTotalElements());

        return "/site/search";
    }
}
