package com.tianyi.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.tianyi.community.annotation.LoginRequired;
import com.tianyi.community.entity.Message;
import com.tianyi.community.entity.Page;
import com.tianyi.community.entity.User;
import com.tianyi.community.event.EventProducer;
import com.tianyi.community.service.MessageService;
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
import org.springframework.web.util.HtmlUtils;

import java.util.*;

@Controller
public class MessageController implements CommunityConstant {
    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @LoginRequired
    @RequestMapping(path = "/message/list", method = RequestMethod.GET)
    public String getConversationList(Model model, Page page) {
        User user = hostHolder.getUser();

        // pagination
        page.setLimit(5);
        page.setPath("/message/list");
        page.setRows(messageService.selectConversationCount(user.getId())); // total # of conversations of the user

        // conversation list
        List<Message> conversationList = messageService.selectConversationsByUserId(
                user.getId(), page.getOffSet(), page.getLimit());
        List<Map<String, Object>> conversations = new ArrayList<>();
        if (conversationList != null) {
            for (Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("unreadCount", messageService.selectUnreadMessageCountByUserId(
                        user.getId(), message.getConversationId()));
                map.put("messageCount", messageService.selectMessageCountByConversation(message.getConversationId()));

                // see if the conversation is initiated by/from the user, target is the opposite
                int targetId = user.getId() == message.getFromId()? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));

                conversations.add(map);
            }
        }
        model.addAttribute("conversations", conversations);

        // get the user's total number of unread messages
        int messageUnreadCount = messageService.selectUnreadMessageCountByUserId(user.getId(), null);
        model.addAttribute("messageUnreadCount", messageUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/message-list";
    }

    @LoginRequired
    @RequestMapping(path="/message/detail/{conversationId}", method = RequestMethod.GET)
    public String getMessageDetail(@PathVariable("conversationId") String conversationId,
                                  Page page, Model model) {
        // pagination
        page.setLimit(5);
        page.setPath("/message/list");
        // total # of conversations of a conversation
        page.setRows(messageService.selectMessageCountByConversation(conversationId));

        // message list
        List<Message> messageList = messageService.selectMessagesByConversation(conversationId, page.getOffSet(), page.getLimit());
        List<Map<String, Object>> messages = new ArrayList<>();
        if (messageList != null) {
            for (Message message : messageList) {
                Map<String, Object> map = new HashMap<>();
                map.put("message", message);
                map.put("fromUser", userService.findUserById(message.getFromId()));
                messages.add(map);
            }
        }
        model.addAttribute("messages", messages);

        // target
        model.addAttribute("target", getMessageTarget(conversationId));

        List<Integer> ids = getMessageIds(messageList);
        if (!ids.isEmpty()) {
            messageService.readMessages(ids);
        }

        return "/site/message-detail";
    }

    private  User getMessageTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if (hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    private List<Integer> getMessageIds(List<Message> messageList) {
        List<Integer> ids = new ArrayList<>();

        if (messageList != null) {
            for (Message message : messageList) {
                // message send to the user and is unread
                if (hostHolder.getUser().getId() == message.getToId() && message.getStatus() == 0) {
                    ids.add(message.getId());
                }
            }
        }
        return ids;
    }

    @LoginRequired
    @RequestMapping(path="/message/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(String toName, String content) {
        User target = userService.findUserByName(toName);
        if (target == null) {
            return CommunityUtil.getJSONString(1, "The user that message sent to does not exist");
        }

        Message message = new Message();
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());

        message.setConversationId(message.getFromId() < message.getToId() ?
                message.getFromId() + "_" + message.getToId()
                : message.getToId() + "_" + message.getFromId());

        message.setContent(content);
        message.setCreateTime(new Date());
        messageService.addMessage(message);
        return CommunityUtil.getJSONString(0);
    }

    /**
     * show notice summary list
     */
    @RequestMapping(path="notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        User user = hostHolder.getUser();

        addNoticeToModelByType(user, TOPIC_COMMENT, model);
        addNoticeToModelByType(user, TOPIC_LIKE, model);
        addNoticeToModelByType(user, TOPIC_FOLLOW, model);

        // find user's total unread message count
        int messageUnreadCount = messageService.selectUnreadMessageCountByUserId(user.getId(), null);
        model.addAttribute("messageUnreadCount", messageUnreadCount);
        // find user's total unread system notice count
        int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "/site/notice";
    }

    private void addNoticeToModelByType(User user, String topic, Model model) {
        // find comment notice
        Message message = messageService.findLatestNotice(user.getId(), topic);
        // related information about the latest message
        if (message != null) {
            Map<String, Object> messageVO = new HashMap<>();
            messageVO.put("message", message);
            // content's " are escaped
            String content = HtmlUtils.htmlUnescape(message.getContent());
            Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

            messageVO.put("user", userService.findUserById((Integer) data.get("userId")));
            messageVO.put("entityType", data.get("entityType"));
            messageVO.put("entityId", data.get("entityId"));
            if (data.get("postId") != null) {
                messageVO.put("postId", data.get("postId"));
            }

            int count = messageService.findNoticeCount(user.getId(), topic);
            messageVO.put("count", count);
            int unread = messageService.findNoticeUnreadCount(user.getId(), topic);
            messageVO.put("unread", unread);
            model.addAttribute(topic + "Notice", messageVO);
        }

    }

    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffSet(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                Map<String, Object> map = new HashMap<>();
                // notice
                map.put("notice", notice);
                // content
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);

                map.put("user", userService.findUserById((Integer)data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                // follow doesn't have postId
                if (data.get("postId") != null) {
                    map.put("postId", data.get("postId"));
                }
                // system user's name
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // set all the messages as read
        List<Integer> ids = getMessageIds(noticeList);
        if (!ids.isEmpty()) {
           messageService.readMessages(ids);
        }

        return "/site/notice-detail";
    }
}
