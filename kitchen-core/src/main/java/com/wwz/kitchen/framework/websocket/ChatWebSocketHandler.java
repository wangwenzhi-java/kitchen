package com.wwz.kitchen.framework.websocket;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwz.kitchen.business.dto.ChatMessageDTO;
import com.wwz.kitchen.business.dto.FriendshipRabbitDto;
import com.wwz.kitchen.business.enums.MessageTypeEnum;
import com.wwz.kitchen.business.service.KitchenFriendshipService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.framework.security.JwtTokenUtil;
import com.wwz.kitchen.persistence.beans.KitchenMenu;
import com.wwz.kitchen.persistence.beans.KitchenPick;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.util.RabbitMqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by wenzhi.wang.
 * on 2024/11/21.
 */
@Component
@ServerEndpoint(value = "/ws/chat/{token}")
@Slf4j
public class ChatWebSocketHandler {
    private static final String ONLINE_KEY = "online:";//存储在线用户的redis key 前缀
    // 使用内存存储 WebSocket 会话，可以根据需要改成 Redis
    private static final Map<Integer, Session> activeSessions = new ConcurrentHashMap<>();
    // 创建 SimpleDateFormat 对象，指定格式
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static RedisTemplate<String, Object> redisTemplate;  // 用于存储在线用户状态
    private static KitchenUsersService kitchenUsersService;
    private static JwtTokenUtil jwtTokenUtil;  // 用于解析和验证 JWT Token
    private static KitchenFriendshipService kitchenFriendshipService;
    private static RabbitMqUtil rabbitMqUtil;

    @Autowired
    public void setRabbitMqUtil(RabbitMqUtil rabbitMqUtil) {
        ChatWebSocketHandler.rabbitMqUtil = rabbitMqUtil;}
    @Autowired
    public void setJwtTokenUtil(JwtTokenUtil jwtTokenUtil) {
        ChatWebSocketHandler.jwtTokenUtil = jwtTokenUtil;
    }
    @Autowired
    public void setKitchenUsersService(KitchenUsersService kitchenUsersService) {
        ChatWebSocketHandler.kitchenUsersService = kitchenUsersService;
    }
    @Autowired
    public void RedisTemplate(RedisTemplate redisTemplate) {
        ChatWebSocketHandler.redisTemplate = redisTemplate;
    }
    @Autowired
    public void KitchenFriendRequestService(KitchenFriendshipService kitchenFriendshipService) {
        ChatWebSocketHandler.kitchenFriendshipService = kitchenFriendshipService;
    }

    /**
     * WebSocket 连接建立时触发
     * @param session WebSocket 会话
     *
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("token") String token) {
        Integer userId = 0;
        boolean b = jwtTokenUtil.validateToken(token);
        String username = jwtTokenUtil.getUsernameFromToken(token);
        if (b && null != username && !"anonymousUser".equals(username) && !"".equals(username) && !"null".equals(username)) {
            QueryWrapper<KitchenUsers> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("username",username);
            KitchenUsers user = kitchenUsersService.getOne(queryWrapper);
            userId = user.getId();
        }
        // Token 无效，关闭 WebSocket 连接
        if (userId == 0) {
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "无效的 Token"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        // 用户上线，记录 WebSocket 会话
        activeSessions.put(userId, session);
        // 更新用户在线状态（存储到 Redis 或内存中）
        redisTemplate.opsForValue().set(ONLINE_KEY + userId, true, 10, TimeUnit.MINUTES);
        List<KitchenUsers> friends = kitchenFriendshipService.getFriend(userId);
        Integer uid = userId;
        friends.stream()
                .filter(friend -> isOnline(friend.getId()))
                .map(friend -> {
                    String jsonString = "";
                    ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
                    chatMessageDTO.setType(MessageTypeEnum.FRIEND_OPEN.getCode());
                    chatMessageDTO.setSender(uid);
                    chatMessageDTO.setSenderName(username);
                    chatMessageDTO.setContent("好友" + username + "上线了！");
                    return new AbstractMap.SimpleEntry<>(friend.getId(), JSON.toJSONString(chatMessageDTO)); // 生成好友ID与消息内容的映射
                })
                .forEach(entry -> sendMessageToClient(entry.getKey(), entry.getValue())); // 推送消息
        System.out.println("用户 " + userId + " 已上线");
    }

    // 判断用户是否在线
    private boolean isOnline(Integer userId) {
        return activeSessions.containsKey(userId);
    }

    /**
     * 接收到客户端消息时触发
     * @param message 消息内容
     * @param session 当前会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        String jsonString = "";
        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
        FriendshipRabbitDto friendshipRabbitDto = new FriendshipRabbitDto();

        JSONObject jsonObject = JSONObject.parseObject(message);
        Integer sendUid = jsonObject.getInteger("sendUid");
        Integer friendId = jsonObject.getJSONObject("data").getInteger("friendId");
        Integer messageType = jsonObject.getInteger("type");
        KitchenUsers kitchenUsers = kitchenUsersService.getById(sendUid);
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.fromValue(messageType);
        switch (messageTypeEnum) {
            case SEND_MESSAGE://消息
                //1.处理实时消息
                String content = jsonObject.getJSONObject("data").getString("content");
                Integer messageContentType = jsonObject.getJSONObject("data").getInteger("messageContentType");
                //1.处理实时消息
                chatMessageDTO.setSender(sendUid);
                chatMessageDTO.setType(MessageTypeEnum.SEND_MESSAGE.getCode());
                if (messageContentType == 3 ||  messageContentType == 5) {//甄选或订单
                    KitchenPick kitchenPick = jsonObject.getJSONObject("data").getObject("pick", KitchenPick.class);
                    chatMessageDTO.setContent(JSON.toJSONString(kitchenPick, SerializerFeature.WriteMapNullValue));
                } else if(messageContentType == 4) {//菜单或服务
                    KitchenMenu kitchenMenu = jsonObject.getJSONObject("data").getObject("menu", KitchenMenu.class);
                    chatMessageDTO.setContent(JSON.toJSONString(kitchenMenu, SerializerFeature.WriteMapNullValue));
                } else {//其他
                    chatMessageDTO.setContent(content);
                }
                chatMessageDTO.setReceiver(friendId);
                chatMessageDTO.setMessageContentType(messageContentType);//文本除了图片和文件 都是文本
                chatMessageDTO.setSendTime(sdf.format(new Date()));
                jsonString = JSON.toJSONString(chatMessageDTO);
                sendMessageToClient(friendId,jsonString);
                // 2异步消息发送
                CompletableFuture.runAsync(() -> rabbitMqUtil.sendToRabbit(messageType, chatMessageDTO));
                break;
            case REQUEST_FRIEND://好友请求
                //1.处理实时消息
                chatMessageDTO.setAvatar(kitchenUsers.getAvatar());
                chatMessageDTO.setSender(sendUid);
                chatMessageDTO.setSenderName(kitchenUsers.getUsername());
                chatMessageDTO.setType(MessageTypeEnum.REQUEST_FRIEND.getCode());
                chatMessageDTO.setContent("");
                chatMessageDTO.setMessageContentType(1);//1.文本 2图片 3甄选 4菜单或服务
                chatMessageDTO.setSendTime(SimpleDateFormat.getDateTimeInstance().format(new Date()));
                jsonString = JSON.toJSONString(chatMessageDTO);
                sendMessageToClient(friendId,jsonString);
                //2.处理异步持久化
                friendshipRabbitDto.setSendUid(sendUid);
                friendshipRabbitDto.setType(MessageTypeEnum.REQUEST_FRIEND.getCode());
                friendshipRabbitDto.setReceiveUid(friendId);
                CompletableFuture.runAsync(() -> rabbitMqUtil.sendToRabbit(messageType,friendshipRabbitDto));
                break;
            case DO_REQUEST_FRIEND://处理好友请求
                //1.处理实时消息
                Boolean accept = jsonObject.getJSONObject("data").getBoolean("accept");
                String username = jsonObject.getJSONObject("data").getString("friendName");
                chatMessageDTO.setType(MessageTypeEnum.DO_REQUEST_FRIEND.getCode());
                if (accept) {//同意了
                    chatMessageDTO.setContent(username + "已同意您的好友请求！");
                } else {
                    chatMessageDTO.setContent(username + "拒绝了您的好友请求！");
                }
                jsonString = JSON.toJSONString(chatMessageDTO);
                sendMessageToClient(friendId,jsonString);
                //2.处理异步持久化
                friendshipRabbitDto.setSendUid(sendUid);
                friendshipRabbitDto.setReceiveUid(friendId);
                friendshipRabbitDto.setType(MessageTypeEnum.DO_REQUEST_FRIEND.getCode());
                friendshipRabbitDto.setAccept(accept);//处理状态 true or false
                CompletableFuture.runAsync(() -> rabbitMqUtil.sendToRabbit(messageType,friendshipRabbitDto));
                break;
            case SHARE_MENU://分享菜单或者服务
                break;
            case SHARE_PICK://分享甄选或订单
                break;
            case REQUEST_SHARE_MENU://请求共享菜单
                //1.处理实时消息
                chatMessageDTO.setAvatar(kitchenUsers.getAvatar());
                chatMessageDTO.setSender(sendUid);
                chatMessageDTO.setSenderName(kitchenUsers.getUsername());
                chatMessageDTO.setType(MessageTypeEnum.REQUEST_SHARE_MENU.getCode());
                chatMessageDTO.setContent("");
                chatMessageDTO.setMessageContentType(1);//1.文本 2图片 3甄选 4菜单或服务 5.订单
                chatMessageDTO.setSendTime(SimpleDateFormat.getDateTimeInstance().format(new Date()));
                jsonString = JSON.toJSONString(chatMessageDTO);
                sendMessageToClient(friendId,jsonString);
                //2.处理异步持久化
                friendshipRabbitDto.setSendUid(sendUid);
                friendshipRabbitDto.setType(MessageTypeEnum.REQUEST_SHARE_MENU.getCode());
                friendshipRabbitDto.setReceiveUid(friendId);
                CompletableFuture.runAsync(() -> rabbitMqUtil.sendToRabbit(messageType,friendshipRabbitDto));
                break;
            case DO_REQUEST_SHARE_MENU://处理共享菜单
                //1.处理实时消息
                Boolean accepted = jsonObject.getJSONObject("data").getBoolean("accept");
                String uame = jsonObject.getJSONObject("data").getString("friendName");
                chatMessageDTO.setType(MessageTypeEnum.DO_REQUEST_SHARE_MENU.getCode());
                if (accepted) {//同意了
                    chatMessageDTO.setContent(uame + "已同意您的共享请求！");
                } else {
                    chatMessageDTO.setContent(uame + "拒绝了您的共享请求！");
                }
                jsonString = JSON.toJSONString(chatMessageDTO);
                sendMessageToClient(friendId,jsonString);
                //2.处理异步持久化
                friendshipRabbitDto.setSendUid(sendUid);//此id是收菜单共享发起人的uid
                friendshipRabbitDto.setReceiveUid(friendId);//此id是发送菜单共享发起人的uid
                friendshipRabbitDto.setType(MessageTypeEnum.DO_REQUEST_SHARE_MENU.getCode());
                friendshipRabbitDto.setAccept(accepted);//处理状态 true or false
                CompletableFuture.runAsync(() -> rabbitMqUtil.sendToRabbit(messageType,friendshipRabbitDto));
                break;
            case DELETE_FRIEND://删除好友的消息
                chatMessageDTO.setType(MessageTypeEnum.DELETE_FRIEND.getCode());
                chatMessageDTO.setSender(sendUid);
                jsonString = JSON.toJSONString(chatMessageDTO);
                sendMessageToClient(friendId,jsonString);
                break;
        }
    }

    /**
     * WebSocket 连接关闭时触发
     * @param session 会话对象
     */
    @OnClose
    public void onClose(Session session) {
        // 找到断开的用户并删除其会话
        Integer userId = activeSessions.entrySet().stream()
                .filter(entry -> entry.getValue().equals(session))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
        if (userId != null) {
            activeSessions.remove(userId);
            // 更新 Redis 中的用户在线状态
            redisTemplate.delete(ONLINE_KEY + userId);
            System.out.println("用户 " + userId + " 已下线");

            List<KitchenUsers> friends = kitchenFriendshipService.getFriend(userId);
            Integer uid = userId;
            KitchenUsers users = kitchenUsersService.getById(uid);
            String username = users == null ? "" : users.getUsername();
            friends.stream()
                    .filter(friend -> isOnline(friend.getId()))
                    .map(friend -> {
                        String jsonString = "";
                        ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
                        chatMessageDTO.setType(MessageTypeEnum.FRIEND_CLOSE.getCode());
                        chatMessageDTO.setSender(uid);
                        chatMessageDTO.setSenderName(username);
                        chatMessageDTO.setContent("好友" + username + "下线了！");
                        return new AbstractMap.SimpleEntry<>(friend.getId(), JSON.toJSONString(chatMessageDTO)); // 生成好友ID与消息内容的映射
                    })
                    .forEach(entry -> sendMessageToClient(entry.getKey(), entry.getValue())); // 推送消息
        }
    }

    /**
     * WebSocket 错误时触发
     * @param session 会话对象
     * @param error 错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket 错误：" + error.getMessage());
    }

    /**
     * 静态方法：向指定客户端发送消息
     */
    public static void sendMessageToClient(Integer uid, String message) {
        Session session = activeSessions.get(uid);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                System.out.println("消息发送成功，内容：" + message);
            } catch (IOException e) {
                System.err.println("消息发送失败，Session ID: " + uid);
                e.printStackTrace();
            }
        } else {
            System.out.println("会话不存在或已关闭，Session ID: " + uid);
        }
    }

    /**
     * 静态方法：向所有客户端广播消息
     */
    public static void broadcastMessage(String message) {
        activeSessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("广播消息失败，Session ID: " + session.getId());
                    e.printStackTrace();
                }
            }
        });
    }

}
