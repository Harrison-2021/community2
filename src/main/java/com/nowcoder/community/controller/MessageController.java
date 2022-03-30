package com.nowcoder.community.controller;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.entity.Page;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
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
    public Message message = null;                 //查询到的最新一条通知信息
    public Map<String, Object> messageVo = null;   // 用来封装聚合数据的map

    @Autowired
    private MessageService messageService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    /** 会话列表显示 */
    @RequestMapping(path = "/letter/list", method = RequestMethod.GET)
    public String getLetterList(Model model, Page page) {
        User user = hostHolder.getUser();

        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/list");
        page.setRows(messageService.findConversationCount(user.getId()));

        // 会话列表
        // 拿到每条会话，同样，与前面帖子，评论类似，将与用户等信息封装在一块，用到List，Map结构
        List<Message> conversationList = messageService.findConversations(
                user.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> conversationVo = new ArrayList<>();
        if(conversationList != null) {
            for(Message message : conversationList) {
                Map<String, Object> map = new HashMap<>();
                map.put("conversation", message);
                map.put("letterCount", messageService.findLetterCount(message.getConversationId()));
                map.put("unreadCount", messageService.findLetterUnreadCount(user.getId(), message.getConversationId()));
                // 查询用户，注意，要查到会话列表中发私信的用户，不是当前用户
                int targetId = user.getId() == message.getFromId() ? message.getToId() : message.getFromId();
                map.put("target", userService.findUserById(targetId));
                conversationVo.add(map);
            }
            model.addAttribute("conversations", conversationVo);

            // 查询所有未读消息数量
            int letterUnreadCount = messageService.findLetterUnreadCount(user.getId(), null);
            model.addAttribute("letterUnreadCount", letterUnreadCount);
            int noticeUnreadCount = messageService.findNoticeUnreadCount(user.getId(), null);
            model.addAttribute("noticeUnreadCount", noticeUnreadCount);
        }

        return "/site/letter";
    }

    /**每个会话中的所有私信显示 */
    @RequestMapping(path = "/letter/detail/{conversationId}", method = RequestMethod.GET)
    public String getLetterDetail(@PathVariable("conversationId") String conversationId, Page page, Model model) {
        // 分页信息
        page.setLimit(5);
        page.setPath("/letter/detail/" + conversationId);
        page.setRows(messageService.findLetterCount(conversationId));

        // 私信列表
        List<Message> letterList = messageService.findLetters(conversationId,page.getOffset(), page.getLimit());
        List<Map<String, Object>> letterVo = new ArrayList<>();
        // 筛选出每个私信列表中未读的私信id，放进list，方便统一更新读取状态，即只要一点开，就全部设为已读
        List<Integer> ids = new ArrayList<>();
        if(letterList != null) {
            // 拿到二级私信列表信息
            for(Message letter : letterList) {
                Map<String, Object> map = new HashMap<>();
                map.put("letter", letter);
                map.put("fromUser", userService.findUserById(letter.getFromId()));
                // 筛选出未读私信，获取id，之后统一处理更新为已读状态
                addUnreadIds(ids, letter);

                letterVo.add(map);
            }
            model.addAttribute("letters", letterVo);
            model.addAttribute("letterCount", messageService.findLetterCount(conversationId));

            // 私信目标，已经遍历完了，可以从之前遍历的里面取一条，如之前操作那样，也可以在遍历外从conversationId中获取
            model.addAttribute("target", getLetterTarget(conversationId));

            // 设置已读状态
            if(!ids.isEmpty()) {
                messageService.updateStatus(ids);
            }
        }

        return "site/letter-detail";
    }

    private User getLetterTarget(String conversationId) {
        String[] ids = conversationId.split("_");
        int id0 = Integer.parseInt(ids[0]);
        int id1 = Integer.parseInt(ids[1]);

        if(hostHolder.getUser().getId() == id0) {
            return userService.findUserById(id1);
        } else {
            return userService.findUserById(id0);
        }
    }

    /**
     * 异步请求，发送私信
     * @param toName   发送给目标用户的用户名
     * @param content  要发送的信息
     * @return          Json字符串
     */
    @RequestMapping(path = "/letter/send", method = RequestMethod.POST)
    @ResponseBody
    public String sendLetter(String toName, String content) {
        // 查询到目标用户-后面需要用户的id
        User target = userService.findUserByName(toName);
        // 先检查客户输入的用户是否存在
        if(target == null) {
            return CommunityUtil.getJSONString(1, "要发送给的目标用户不存在!");
        }
        // 添加私信数据，注意，status默认为0， 即未读状态
        Message message = new Message();
        message.setContent(content);
        message.setFromId(hostHolder.getUser().getId());
        message.setToId(target.getId());
        // conversationId，小的数在前，大的数在后
        if(message.getFromId() < message.getToId()) {
            message.setConversationId(message.getFromId() + "_" + message.getToId());
        } else {
            message.setConversationId(message.getToId() + "_" + message.getFromId());
        }
        message.setCreateTime(new Date());
        messageService.addMessage(message);

        return CommunityUtil.getJSONString(0, "发送私信成功!");
    }

    /**
     * 查询系统通知列表的请求
     * @param model
     * @return
     */
    @RequestMapping(value = "/notice/list", method = RequestMethod.GET)
    public String getNoticeList(Model model) {
        // 1.先获取当前登录用户
        User user = hostHolder.getUser();
        int userId = user.getId();
        // 2.显示评论总体信息
        addData(userId, TOPIC_COMMENT);
        model.addAttribute("commentNotice", messageVo);
        // 3. 显示点赞信息
        addData(userId, TOPIC_LIKE);
        model.addAttribute("likeNotice", messageVo);
        // 4. 显示关注信息
        addData(userId, TOPIC_FOLLOW);
        model.addAttribute("followNotice", messageVo);
        // 5. 查询总的未读消息数量
        int letterUnreadCount = messageService.findLetterUnreadCount(userId, null);
        model.addAttribute("letterUnreadCount", letterUnreadCount);
        int noticeUnreadCount = messageService.findNoticeUnreadCount(userId, null);
        model.addAttribute("noticeUnreadCount", noticeUnreadCount);

        return "site/notice";
    }

    /**
     * 显示通知列表详情
     * @param topic
     * @param page
     * @param model
     * @return
     */
    @RequestMapping(path = "/notice/detail/{topic}", method = RequestMethod.GET)
    public String getNoticeDetail(@PathVariable("topic") String topic, Page page, Model model) {
        User user = hostHolder.getUser();

        page.setLimit(5);
        page.setPath("/notice/detail/" + topic);
        page.setRows(messageService.findNoticeCount(user.getId(), topic));

        List<Message> noticeList = messageService.findNotices(user.getId(), topic, page.getOffset(), page.getLimit());
        List<Map<String, Object>> noticeVoList = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        if (noticeList != null) {
            for (Message notice : noticeList) {
                // 先筛选出未读信息
                addUnreadIds(ids,notice);

                Map<String, Object> map = new HashMap<>();
                // 通知
                map.put("notice", notice);
                // 内容
                String content = HtmlUtils.htmlUnescape(notice.getContent());
                Map<String, Object> data = JSONObject.parseObject(content, HashMap.class);
                map.put("user", userService.findUserById((Integer) data.get("userId")));
                map.put("entityType", data.get("entityType"));
                map.put("entityId", data.get("entityId"));
                map.put("postId", data.get("postId"));
                // 通知作者-也就是系统
                map.put("fromUser", userService.findUserById(notice.getFromId()));

                noticeVoList.add(map);
            }
        }
        model.addAttribute("notices", noticeVoList);

        // 设置已读
        if (!ids.isEmpty()) {
            messageService.updateStatus(ids);
        }

        return "/site/notice-detail";
    }

    /**
     * 统计未读消息的id
     * 与查询逻辑一致，是发送对象为当前用户的私信且状态为0
     * @param ids       收集未读消息id的列表
     * @param message   要判定的每个消息对象
     */
    private void addUnreadIds(List<Integer> ids, Message message) {
        if(message.getToId() == hostHolder.getUser().getId() && message.getStatus() == 0) {
            ids.add(message.getId());
        }
    }

    /**
     * 封装处理系统通知封装数据的代码
     * @param userId          当前登录的用户id
     * @param topic           主题
     */
    private void addData(int userId, String topic) {
        message = messageService.findLatestNotice(userId, topic);
        messageVo = new HashMap<>();
        if(message != null) {
            // 装进最新一条评论信息
            messageVo.put("message", message);
            // 将content内的json字符串恢复成对象，方便处理
            // 先将转义字符反转成正常字符
            message.setContent(HtmlUtils.htmlUnescape(message.getContent()));
            Map<String, Object> data = JSONObject.parseObject(message.getContent(), Map.class);
            // 触发者信息
            messageVo.put("fromUser", userService.findUserById((Integer) data.get("userId")));
            // 触发对象信息
            messageVo.put("entityType", data.get("entityType"));
            messageVo.put("entityId", data.get("entityId"));
            Integer postId = (Integer)data.get("postId");
            if(postId != null) {    // 关注类通知没有这个数据，因为不需要
                messageVo.put("postId", postId);
            }
            // 消息总数
            int count = messageService.findNoticeCount(userId, topic);
            messageVo.put("count", count);
            // 未读消息数量
            int unread = messageService.findNoticeUnreadCount(userId, topic);
            messageVo.put("unread", unread);
        }
    }
}
