package com.tianyi.community.dao;

import com.tianyi.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    
    // paged query
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit, int orderMode); // dynamic sql query, use userId when show post in user's personal page

    // get the number of posts of the user
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int

    updateCommentCount(int id, int commentCount);

    int updateType(int id, int type);

    int updateStatus(int id, int status);

    int updateScore(int id, double score);
}
