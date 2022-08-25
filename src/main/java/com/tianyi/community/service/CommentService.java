package com.tianyi.community.service;


import com.tianyi.community.dao.CommentMapper;
import com.tianyi.community.dao.DiscussPostMapper;
import com.tianyi.community.entity.Comment;
import com.tianyi.community.util.CommunityConstant;
import com.tianyi.community.util.CommunityUtil;
import com.tianyi.community.util.WordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private WordFilter wordFilter;

    public List<Comment> findCommentByEntity(int entityType, int entityId, int offset, int limit)  {
        return commentMapper.selectCommentsByEntity(entityType, entityId, offset, limit);
    }

    public int findCommentCount(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Argument can't be null");
        }

        // Add comment
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(wordFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        // update the post's comment counts
        if (comment.getEntityType() == ENTITY_TYPE_POST) {
            // get current number of comments to the post
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }
}