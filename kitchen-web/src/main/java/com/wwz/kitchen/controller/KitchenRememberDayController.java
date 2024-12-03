package com.wwz.kitchen.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwz.kitchen.business.dto.KitchenUsersRegistryDTO;
import com.wwz.kitchen.business.dto.RememberDayDTO;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.business.enums.StatusEnum;
import com.wwz.kitchen.business.service.KitchenRememberDayService;
import com.wwz.kitchen.framework.annotation.KitchenLogs;
import com.wwz.kitchen.persistence.beans.KitchenPick;
import com.wwz.kitchen.persistence.beans.KitchenRememberDay;
import com.wwz.kitchen.util.AjaxResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

/**
 * Created by wenzhi.wang.
 * on 2024/11/20.
 * 纪念日管理
 */
@RestController
@RequestMapping("/days")
@Slf4j
public class KitchenRememberDayController extends BaseController{

    @Autowired
    private KitchenRememberDayService kitchenRememberDayService;

    @PostMapping("addDay")
    @KitchenLogs(value = "用户添加纪念日", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String addDay(@RequestBody @Valid RememberDayDTO rememberDayDTO, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) { //没登录
                return buildResponse(res,AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            KitchenRememberDay kitchenRememberDay = new KitchenRememberDay();
            kitchenRememberDay.setUid(uid);
            kitchenRememberDay.setRememberDay(rememberDayDTO.getFormattedRememberDay());
            kitchenRememberDay.setStatus(StatusEnum.RELEASE.toString());
            kitchenRememberDay.setTitle(rememberDayDTO.getTitle());
            boolean save = kitchenRememberDayService.saveRememberDay(kitchenRememberDay);
            if (save) {
                res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
        } catch (Exception e) {
            log.error("KitchenRememberDayController===addDay:{}", e.getMessage());
        }
        return AjaxResultUtil.response(res,AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
    }

    @GetMapping("/loadRememberDays")
    @KitchenLogs(value = "登录用户加载纪念日", platform = PlatformEnum.KITCHEN,save = false)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String loadRememberDays(HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) { //没登录
                return buildResponse(res,AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            List<KitchenRememberDay> list = kitchenRememberDayService.listByUid(uid);
            res.put("days",list);
            res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("KitchenRememberDayController===loadRememberDays:{}", e.getMessage());
        }
        return AjaxResultUtil.response(res,AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
    }

    @DeleteMapping("/deleteRememberDay/{rid}")
    @KitchenLogs(value = "登录用户删除纪念日", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String deleteRememberDay(@PathVariable Integer rid , HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) { //没登录
                return buildResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            KitchenRememberDay kitchenRememberDay = kitchenRememberDayService.getByIdFromDb(rid);
            if (kitchenRememberDay == null) {
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
            }
            kitchenRememberDay.setStatus(StatusEnum.NOT_RELEASE.toString());
            boolean b = kitchenRememberDayService.updateRememberDayById(kitchenRememberDay);
            if (b) {
                res.put("msg",AjaxResponseCodeEnum.SUCCESS.getMessage());
                return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
            }
        } catch (Exception e) {
            log.error("KitchenRememberDayController===deleteRememberDay:{}", e.getMessage());
        }
        return AjaxResultUtil.response(res,AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
    }

    // 构建错误响应
    private String buildResponse(JSONObject res, AjaxResponseCodeEnum code) {
        res.put("msg", code.getMessage());
        return AjaxResultUtil.response(res, code.getCode());
    }
}
