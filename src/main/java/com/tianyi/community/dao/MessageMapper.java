package com.tianyi.community.dao;

import com.tianyi.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // get current user's conversation list, only return the last message for each conversation
    List<Message> selectConversationsByUserId(int userId, int offset, int limit);

    // get user's total num of the conversations
    int selectConversationCount(int userId);

    // get all messages of a conversation
    List<Message> selectMessagesByConversation(String conversationId, int offset, int limit);

    // get count of the messages of a conversation
    int selectMessageCountByConversation(String conversationId);

    // get count of unread messages to a user of a particular conversation
    int selectUnreadMessageCountByUserId(int userId, String conversationId);

    int insertMessage(Message message);

    // when the message is shown to the user, need to update messages in a batch
    int updateMessagesStatus(List<Integer> messageIds, int status);

    // search the newest notice under a topic
    Message selectLatestNotice(int userId, String topic);

    // search the number of notice under a topic
    int selectNoticeCount(int userId, String topic);

    // search the number of unread notice under a topic
    int selectUnreadNoticeCount(int userId, String topic);

    // search the notices under a topic of a user
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
