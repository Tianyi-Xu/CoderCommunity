package com.tianyi.community.dao;

import com.tianyi.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    // offset and limit is used for page divition
    List<Comment> selectCommentsByEntity(int entityType, int entityId, int offset, int limit);

    // get the total number for the comment in order to divide into pages
    int selectCountByEntity(int entityType, int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);


}
