package com.wwz.kitchen.controller;

import com.alibaba.fastjson.JSONObject;
import com.wwz.kitchen.business.dto.KitchenTopicDTO;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.business.service.KitchenTopicService;
import com.wwz.kitchen.framework.annotation.KitchenLogs;
import com.wwz.kitchen.framework.qinqiu.QiniuService;
import com.wwz.kitchen.util.AjaxResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.listener.Topic;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.PrivilegedAction;

/**
 * Created by wenzhi.wang.
 * on 2024/12/5.
 */
@RestController()
@RequestMapping("/topic")
@Slf4j
public class TopicController extends BaseController{

    @Autowired
    private QiniuService qiniuService;

    @Autowired
    private KitchenTopicService kitchenTopicService;
    /**
     * 帖子图片
     * @param file
     * @param request
     * @return
     */
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    @PostMapping("/uploadTopicImage")
    @KitchenLogs(value = "用户上传帖子图片到七牛云", platform = PlatformEnum.CHAT)
    public String uploadTopicImage(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
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
            log.error("上传图片异常", e);
        }
        return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    /**
     * 发帖
     */
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    @PostMapping("/publishTopic")
    @KitchenLogs(value = "用户发布帖子", platform = PlatformEnum.CHAT)
    public String publishTopic(@RequestBody KitchenTopicDTO kitchenTopicDTO, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return AjaxResultUtil.buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            Integer topicId = kitchenTopicService.pubTopic(kitchenTopicDTO,uid);
            if (topicId > 0) {//返回帖子内容
                KitchenTopicDTO kitchenTopicDTO1 = kitchenTopicService.getTopicByTopicId(topicId);
            }
        } catch (Exception e) {

        }
    }
}
