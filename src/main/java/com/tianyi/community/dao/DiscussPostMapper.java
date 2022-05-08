package com.tianyi.community.dao;

import com.tianyi.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    
    // paged query
    List<DiscussPost> selectDiscussPosts(int userId, int offset, int limit); // dynamic sql query, use userId when show post in user's personal page

    // int
    int selectDiscussPostRows(@Param("userId") int userId);

}
