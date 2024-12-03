package com.wwz.kitchen.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiniu.util.Json;
import com.wwz.kitchen.business.dto.ChatMessageDTO;
import com.wwz.kitchen.business.dto.ChatUserDto;

import com.wwz.kitchen.business.enums.*;
import com.wwz.kitchen.business.service.*;
import com.wwz.kitchen.framework.annotation.KitchenLogs;
import com.wwz.kitchen.framework.qinqiu.QiniuService;
import com.wwz.kitchen.persistence.beans.KitchenFriendRequest;
import com.wwz.kitchen.persistence.beans.KitchenFriendship;

import com.wwz.kitchen.persistence.beans.KitchenMenusShare;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.util.AjaxResultUtil;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;


import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.druid.sql.visitor.SQLEvalVisitorUtils.eq;
import static com.wwz.kitchen.business.enums.FriendshipStatusEnum.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;

/**
 * Created by wenzhi.wang.
 * on 2024/11/23.
 */
@RestController
@RequestMapping("/friend")
@Slf4j
public class FriendController extends BaseController{

    @Autowired
    private KitchenFriendshipService kitchenFriendshipService;
    @Autowired
    private KitchenFriendRequestService kitchenFriendRequestService;
    @Autowired
    private KitchenMessageService kitchenMessageService;
    @Autowired
    private KitchenMenusShareService kitchenMenusShareService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;  // 用于存储在线用户状态
    @Autowired
    private QiniuService qiniuService;
    private static final String ONLINE_KEY = "online:";//存储在线用户的redis key 前缀

    @GetMapping("searchFriendByName/{friend}")
    @KitchenLogs(value = "用户通过用户名搜索好友",platform = PlatformEnum.CHAT)
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    public String searchFriendByName(@PathVariable("friend") String name, HttpServletRequest request){
        log.info("searchFriendByName - " + name);
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            QueryWrapper<KitchenUsers> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(KitchenUsers::getUsername, name);
            queryWrapper.lambda().eq(KitchenUsers::getStatus, StatusEnum.RELEASE.toString());
            KitchenUsers friend = super.kitchenUsersService.getOne(queryWrapper);
            if (friend == null) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.USERNAME_NOT_EXIST_ERROR);
            }
            Integer friendUid = friend.getId();
            if (uid == friendUid) {
                res.put("msg","你搜你自己是脑残吗？");
                return AjaxResultUtil.response(res, AjaxResponseCodeEnum.OPERATION_FAILED.getCode());
            }
            Boolean isOnline = (Boolean) redisTemplate.opsForValue().get(ONLINE_KEY + friendUid);
            ChatUserDto chatUserDto = new ChatUserDto();
            chatUserDto.setId(friendUid);
            chatUserDto.setUsername(friend.getUsername());
            chatUserDto.setAvatar(friend.getAvatar());
            if (isOnline != null && isOnline) {
                chatUserDto.setIsOnline(ChatUserOnlineStatusEnum.ONLINE.getCode());
            } else {
                chatUserDto.setIsOnline(ChatUserOnlineStatusEnum.OFFINE.getCode());
            }

            //这里要查双向好友关系 虽然我的逻辑都是双加双删。。但是我手贱 就想写 方便以后扩展
            QueryWrapper<KitchenFriendship> friendshipQueryWrapper = new QueryWrapper<>();
            friendshipQueryWrapper.lambda().eq(KitchenFriendship::getUid, uid);
            friendshipQueryWrapper.lambda().eq(KitchenFriendship::getFriendId, friendUid);
            friendshipQueryWrapper.lambda().eq(KitchenFriendship::getStatus, FriendshipStatusEnum.CONFIRMED.getValue());
            KitchenFriendship friendship = kitchenFriendshipService.getOne(friendshipQueryWrapper);
            QueryWrapper<KitchenFriendship> friendshipQueryWrapper1 = new QueryWrapper<>();
            friendshipQueryWrapper1.lambda().eq(KitchenFriendship::getUid,friendUid);
            friendshipQueryWrapper1.lambda().eq(KitchenFriendship::getFriendId, uid);
            friendshipQueryWrapper1.lambda().eq(KitchenFriendship::getStatus, FriendshipStatusEnum.CONFIRMED.getValue());
            KitchenFriendship friendship1 = kitchenFriendshipService.getOne(friendshipQueryWrapper1);

            if (friendship != null && friendship1 != null) {//已经是好友
                chatUserDto.setStatus(FriendshipStatusEnum.CONFIRMED.getValue());
                res.put("friend", chatUserDto);
                res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.SUCCESS);
            }

            QueryWrapper<KitchenFriendRequest> friendRequestQueryWrapper = new QueryWrapper<>();
            friendRequestQueryWrapper.lambda().eq(KitchenFriendRequest::getSenderId,uid);
            friendRequestQueryWrapper.lambda().eq(KitchenFriendRequest::getReceiverId,friendUid);
            KitchenFriendRequest kitchenFriendRequest = kitchenFriendRequestService.getOne(friendRequestQueryWrapper);
            if (kitchenFriendRequest == null) {//没有发起请求
                chatUserDto.setStatus(520);
                res.put("friend", chatUserDto);
                res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.SUCCESS);
            }

            FriendshipStatusEnum friendshipStatusEnum =  FriendshipStatusEnum.fromValue(kitchenFriendRequest.getStatus());
            switch (friendshipStatusEnum){
                case TO_BE_CONFIRMED:
                    chatUserDto.setStatus(TO_BE_CONFIRMED.getValue());
                    break;
                case CONFIRMED:
                    chatUserDto.setStatus(CONFIRMED.getValue());
                    break;
                case REFUSE:
                    chatUserDto.setStatus(REFUSE.getValue());
                    break;
                default:
                    chatUserDto.setStatus(520);
                    break;
            }
            res.put("friend", chatUserDto);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.SUCCESS);
        } catch (Exception e) {
            log.error("FriendController========searchFriendByName:{}",e.getMessage());
        }
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    //初始化喵友页面好友请求
    @GetMapping("getFriendRequest")
    @KitchenLogs(value = "初始化喵友页面好友请求",platform = PlatformEnum.CHAT)
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    public String getFriendRequest(HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            QueryWrapper<KitchenFriendRequest> friendRequestQueryWrapper = new QueryWrapper<>();
            friendRequestQueryWrapper.lambda().eq(KitchenFriendRequest::getReceiverId,uid);
            friendRequestQueryWrapper.lambda().eq(KitchenFriendRequest::getStatus, FriendshipStatusEnum.TO_BE_CONFIRMED.getValue());
            List<KitchenFriendRequest> requestList = kitchenFriendRequestService.list(friendRequestQueryWrapper);
            if (requestList == null || requestList.isEmpty()) {
                res.put("friendRequestList",new ArrayList<>());
                res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
            List<Integer> userIds = requestList.stream()
                    .map(KitchenFriendRequest::getSenderId)  // 获取 senderId
                    .collect(Collectors.toList());

            QueryWrapper<KitchenUsers> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(KitchenUsers::getId, userIds); // 使用 in 查询多个 userId

            List<KitchenUsers> users = kitchenUsersService.list(queryWrapper);

            if (users == null || users.isEmpty()) {
                res.put("friendRequestList",new ArrayList<>());
                res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
            List<ChatMessageDTO> chatMessageDTOList = users.stream().map(kitchenUser -> {
                ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
                chatMessageDTO.setSender(kitchenUser.getId());
                chatMessageDTO.setAvatar(kitchenUser.getAvatar());
                chatMessageDTO.setSenderName(kitchenUser.getUsername());
                // 匹配请求时间
                Optional<KitchenFriendRequest> matchedRequest = requestList.stream()
                        .filter(requestFriend -> requestFriend.getSenderId().equals(kitchenUser.getId())) // 匹配逻辑
                        .findFirst();
                String sendTime = matchedRequest
                        .map(requestFriend -> SimpleDateFormat.getDateInstance().format(requestFriend.getCreateTime()))
                        .orElse("未知时间"); // 替换成你的
                chatMessageDTO.setSendTime(sendTime);
                return chatMessageDTO;
            }).collect(Collectors.toList());
            res.put("friendRequestList", chatMessageDTOList);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("FriendController========getFriendRequest:{}",e.getMessage());
        }
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }
    //初始化喵友页面好友请求
    @GetMapping("getFriends")
    @KitchenLogs(value = "加载好友列表",platform = PlatformEnum.CHAT)
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
    public String getFriends(HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            List<KitchenUsers> sortedKitchenUsers = kitchenFriendshipService.getFriend(uid);
            List<ChatUserDto> chatUserDtoList = sortedKitchenUsers.stream()
                    .map(user -> {
                ChatUserDto chatUserDto = new ChatUserDto();
                chatUserDto.setId(user.getId());
                chatUserDto.setUsername(user.getUsername());
                chatUserDto.setAvatar(user.getAvatar());
                chatUserDto.setUnreadMessages(kitchenMessageService.getUnreadMessagesByFriendId(uid,user.getId()));//查询未读消息总数
                Boolean isOnline = (Boolean) redisTemplate.opsForValue().get(ONLINE_KEY + user.getId());
                chatUserDto.setIsOnline(Boolean.TRUE.equals(isOnline) ? 1 : 0);
                return chatUserDto;
            })
            .collect(Collectors.toList());

            res.put("friends", chatUserDtoList);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("FriendController========getFriends:{}",e.getMessage());
        }
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("actionFriend/{action}/{friendId}")
    @KitchenLogs(value = "置顶或删除好友",platform = PlatformEnum.CHAT)
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET,PUT,DELETE},allowCredentials = "true")
    public String actionFriend(@PathVariable("action") String action, @PathVariable("friendId") Integer friendId,HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            QueryWrapper<KitchenFriendship> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(KitchenFriendship::getFriendId, friendId);
            queryWrapper.lambda().eq(KitchenFriendship::getUid,uid);
            queryWrapper.lambda().eq(KitchenFriendship::getStatus, FriendshipStatusEnum.CONFIRMED.getValue());
            KitchenFriendship kitchenFriendship = kitchenFriendshipService.getOne(queryWrapper);
            if (kitchenFriendship == null) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.DATA_NOT_EXIST);
            }
            if ("Pinned".equals(action)) {
                kitchenFriendship.setSort(1);
                boolean b = kitchenFriendshipService.updateById(kitchenFriendship);
                if (b) {
                    res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                    return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
                }
            }
            if ("Del".equals(action)) {
                boolean result = kitchenFriendshipService.deleteFriendship(uid, friendId);
                if (result) {
                    res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                    return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
                }
            }
        } catch (Exception e) {
            log.error("FriendController========actionFriend:{}",e.getMessage());
        }
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/getChatHistory")
    @KitchenLogs(value = "获取历史消息",platform = PlatformEnum.CHAT)
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET,PUT,DELETE},allowCredentials = "true")
    public String getChatHistory(@RequestParam("receiverId") Integer friendId,
                                 @RequestParam("lastMessageId") Integer lastMessageId,  // 使用 Unix 时间戳
                                 @RequestParam("size") int size,
                                 HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            //分页加载历史消息
            Page<ChatMessageDTO> chatHistory = kitchenMessageService.getChatHistoryMessage(uid,friendId,lastMessageId,size);
            res.put("pages", chatHistory);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("FriendController========getChatHistory:{}",e.getMessage());
        }
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    @PostMapping("/uploadChatImage")
    @KitchenLogs(value = "用户上传聊天图片到七牛云", platform = PlatformEnum.CHAT)
    public String uploadChatImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            String url = qiniuService.uploadChatImage(file, request);
            if (StringUtils.isEmpty(url)) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.USER_AVATAR_ERROR);
            }
            // 更新用户头像
            res.put("url",url);
            res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (IOException e) {
            log.error("上传头像异常", e);
        }
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    @PutMapping("readMessages")
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET,PUT}, allowCredentials = "true")
    @KitchenLogs(value = "用户读取未读消息", platform = PlatformEnum.CHAT)
    public String readMessages(@RequestParam Integer senderId, @RequestParam Integer receiverId,HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            if (senderId == null || senderId == 0 || receiverId == null || receiverId == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.VALIDATION_ERROR);
            }
            boolean b =  kitchenMessageService.readMessages(senderId,receiverId);

        } catch (Exception e) {
            log.error("FriendController========readMessages:{}",e.getMessage());
        }
        return AjaxResultUtil.buildResponse(res,AjaxResponseCodeEnum.SUCCESS);
    }

    /**
     * 双向查询！
     * @param friendId
     * @param request
     * @return
     */
    @GetMapping("isShare/{friendId}")
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET,PUT}, allowCredentials = "true")
    @KitchenLogs(value = "是否已共享菜单？", platform = PlatformEnum.CHAT)
    public String isShare(@PathVariable Integer friendId, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            QueryWrapper<KitchenMenusShare> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .in(KitchenMenusShare::getShareFromUid, uid, friendId)  // 使用in匹配ShareFromUid
                    .in(KitchenMenusShare::getShareToUid, uid, friendId)    // 使用in匹配ShareToUid
                    .and(wrapper -> wrapper
                            .in(KitchenMenusShare::getStatus, ShareStatusEnum.NOT_CONFIRM.getCode(), ShareStatusEnum.CONFIRM.getCode())  // 匹配状态
                    );
            List<KitchenMenusShare> list = kitchenMenusShareService.list(queryWrapper);
            if (list == null || list.size() == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.SUCCESS);
            }
            return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.HAS_SHARED);
        } catch (Exception e) {
            log.error("FriendController========isShare:{}",e.getMessage());
        }
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("getFriendShareRequest")
    @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET,PUT}, allowCredentials = "true")
    @KitchenLogs(value = "获取未处理分享请求", platform = PlatformEnum.CHAT)
    public String getFriendShareRequest(HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            QueryWrapper<KitchenMenusShare> queryWrapperKitchenMenusShare = new QueryWrapper<>();
            queryWrapperKitchenMenusShare.lambda().eq(KitchenMenusShare::getShareToUid, uid);
            queryWrapperKitchenMenusShare.lambda().eq(KitchenMenusShare::getStatus,ShareStatusEnum.NOT_CONFIRM.getCode());
            List<KitchenMenusShare> kitchenMenusShareList = kitchenMenusShareService.list(queryWrapperKitchenMenusShare);
            if (kitchenMenusShareList == null || kitchenMenusShareList.isEmpty()) {
                res.put("friendRequestShareList",new ArrayList<>());
                res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
            List<Integer> userIds = kitchenMenusShareList.stream()
                    .map(KitchenMenusShare::getShareFromUid)  // 获取 senderId
                    .collect(Collectors.toList());
            QueryWrapper<KitchenUsers> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(KitchenUsers::getId, userIds); // 使用 in 查询多个 userId
            List<KitchenUsers> users = kitchenUsersService.list(queryWrapper);
            if (users == null || users.isEmpty()) {
                res.put("friendRequestShareList",new ArrayList<>());
                res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
            List<ChatMessageDTO> chatMessageDTOList = users.stream().map(kitchenUser -> {
                ChatMessageDTO chatMessageDTO = new ChatMessageDTO();
                chatMessageDTO.setSender(kitchenUser.getId());
                chatMessageDTO.setAvatar(kitchenUser.getAvatar());
                chatMessageDTO.setSenderName(kitchenUser.getUsername());
                // 匹配请求时间
                Optional<KitchenMenusShare> matchedRequest = kitchenMenusShareList.stream()
                        .filter(requestShareFriend -> requestShareFriend.getShareFromUid().equals(kitchenUser.getId())) // 匹配逻辑
                        .findFirst();
                String sendTime = matchedRequest
                        .map(requestShareFriend -> SimpleDateFormat.getDateInstance().format(requestShareFriend.getCreateTime()))
                        .orElse("未知时间"); // 替换成你的
                chatMessageDTO.setSendTime(sendTime);
                return chatMessageDTO;
            }).collect(Collectors.toList());
            res.put("friendRequestShareList", chatMessageDTOList);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("FriendController========getFriendShareRequest:{}",e.getMessage());
        }
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);

    }

}
