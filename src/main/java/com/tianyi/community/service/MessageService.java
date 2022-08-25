package com.tianyi.community.service;

import com.tianyi.community.dao.MessageMapper;
import com.tianyi.community.entity.Message;
import com.tianyi.community.util.WordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private WordFilter wordFilter;

    // get current user's conversation list, only return the last message for each conversation
    public List<Message> selectConversationsByUserId(int userId, int offset, int limit) {
        return messageMapper.selectConversationsByUserId(userId, offset, limit);
    }

    // get user's total num of the conversations
    public int selectConversationCount(int userId) {
        return messageMapper.selectConversationCount(userId);
    }

    // get all messages of a conversation
    public List<Message> selectMessagesByConversation(String conversationId, int offset, int limit) {
        return messageMapper.selectMessagesByConversation(conversationId, offset, limit);
    }

    // get count of the messages of a conversation
    public int selectMessageCountByConversation(String conversationId){
        return messageMapper.selectMessageCountByConversation(conversationId);
    }

    // get count of unread messages to a user of a particular conversation
    public int selectUnreadMessageCountByUserId(int userId, String conversationId) {
        return messageMapper.selectUnreadMessageCountByUserId(userId, conversationId);
    }

    public int addMessage(Message message) {
        message.setContent(HtmlUtils.htmlEscape(message.getContent()));
        message.setContent(wordFilter.filter(message.getContent()));
        return messageMapper.insertMessage(message);
    }

    public int readMessages(List<Integer> messageIds) {
        return messageMapper.updateMessagesStatus(messageIds, 1);
    }

    public Message findLatestNotice(int userId, String topic) {
        return messageMapper.selectLatestNotice(userId, topic);
    }

    public int findNoticeCount(int userId, String topic) {
        return messageMapper.selectNoticeCount(userId, topic);
    }

    public int findNoticeUnreadCount(int userId, String topic) {
        return messageMapper.selectUnreadNoticeCount(userId, topic);
    }

    public List<Message> findNotices(int userId, String topic, int offset, int limit) {
        return messageMapper.selectNotices(userId, topic, offset, limit);
    }
}
