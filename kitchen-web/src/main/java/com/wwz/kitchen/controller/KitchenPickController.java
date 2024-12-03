package com.wwz.kitchen.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wwz.kitchen.business.dto.KitchenPickDTO;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.business.enums.StatusEnum;
import com.wwz.kitchen.business.service.KitchenMenuService;
import com.wwz.kitchen.business.service.KitchenPickMenuService;
import com.wwz.kitchen.business.service.KitchenPickService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.framework.annotation.KitchenLogs;
import com.wwz.kitchen.framework.security.JwtTokenUtil;
import com.wwz.kitchen.persistence.beans.KitchenMenu;
import com.wwz.kitchen.persistence.beans.KitchenPick;
import com.wwz.kitchen.persistence.beans.KitchenPickMenu;
import com.wwz.kitchen.util.AjaxResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 甄选相关
 * Created by wenzhi.wang.
 * on 2024/11/19.
 */
@RestController
@RequestMapping("/pick")
@Slf4j
public class KitchenPickController extends BaseController{
    @Autowired
    private KitchenPickService kitchenPickService;
    @Autowired
    private KitchenPickMenuService kitchenPickMenuService;
    @Autowired
    private KitchenMenuService kitchenMenuService;

    /**
     *
     * @param kitchenPickDTO
     * @param request
     * @return
     */
    @PostMapping("addPick")
    @KitchenLogs(value = "甄选或菜单下单", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String addPick(@RequestBody KitchenPickDTO kitchenPickDTO , HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) { //未登录
                return buildErrorResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            // 此处有事务处理  在service中处理
            String result = kitchenPickService.addPick(kitchenPickDTO, uid, request);
            return result;
        } catch (Exception e) {
            log.error("KitchenPickController===addPick:{}", e.getMessage());
            return buildErrorResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/loadPicksByLoginUser")
    @KitchenLogs(value = "甄选列表，分页数据", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String loadPicksByLoginUser(@RequestParam("type") Integer type, @RequestParam("page") Integer current,@RequestParam("pageSize") Integer pageSize, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
           Integer uid = getUid(request);
           if (uid == 0) {
               return buildErrorResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
           }
            // 创建 Page 对象，当前页码，页大小
            Page<KitchenPick> page = new Page<>(current, pageSize);
            Page<KitchenPick> pages = kitchenPickService.page(page, new QueryWrapper<KitchenPick>().lambda()
                    .eq(KitchenPick::getStatus, StatusEnum.RELEASE.toString())
                    .eq(KitchenPick::getUid, uid)
                    .eq(KitchenPick::getType, type)
                    .orderByDesc(KitchenPick::getCreateTime));
            res.put("data", pages.getRecords());
            res.put("total", pages.getTotal());
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("KitchenPickController===loadPicksByLoginUser:{}", e.getMessage());
        }
        res.put("data", "");
        res.put("total", 0);
        res.put("msg", AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
        return AjaxResultUtil.response(res,AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
    }

    @GetMapping("/loadPickMenusByPickId/{pid}")
    @KitchenLogs(value = "登录用户根据pickId获取菜单或服务列表", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String loadPickMenusByPickId(@PathVariable Integer pid,HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            QueryWrapper<KitchenPickMenu> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(KitchenPickMenu::getPickId, pid);
            queryWrapper.lambda().eq(KitchenPickMenu::getStatus, StatusEnum.RELEASE.toString());
            List<KitchenPickMenu> list = kitchenPickMenuService.list(queryWrapper);
            System.out.println(list.size());
            List<Integer> menusIds = new ArrayList<>();
            list.forEach(kitchenPickMenu -> {
                menusIds.add(kitchenPickMenu.getMenuId());
            });
            List<KitchenMenu> kitchenMenus = kitchenMenuService.listByIds(menusIds);
            res.put("pid",pid);
            res.put("menus", kitchenMenus);
            res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
            return AjaxResultUtil.response(res,AjaxResponseCodeEnum.SUCCESS.getCode());
        } catch (Exception e) {
            log.error("KitchenPickController===loadPickMenusByPickId:{}", e.getMessage());
        }
        res.put("pid","");
        res.put("menus", "");
        res.put("msg", AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
        return AjaxResultUtil.response(res,AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
    }

    @PutMapping("del/{pickId}")
    @KitchenLogs(value = "删除订单或甄选", platform = PlatformEnum.KITCHEN)
    @CrossOrigin(origins = "http://localhost:8080", methods = {RequestMethod.POST, RequestMethod.GET}, allowCredentials = "true")
    public String del(@PathVariable Integer pickId,HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Integer uid = getUid(request);
            if (uid == 0) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.UNAUTHORIZED);
            }
            QueryWrapper<KitchenPick>  queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda()
                    .eq(KitchenPick::getId, pickId)
                    .eq(KitchenPick::getUid,uid)
                    .eq(KitchenPick::getStatus, StatusEnum.RELEASE.toString());
            KitchenPick kitchenPick = kitchenPickService.getOne(queryWrapper);
            if (kitchenPick != null) {
                kitchenPick.setStatus(StatusEnum.NOT_RELEASE.toString());
                boolean b = kitchenPickService.updateById(kitchenPick);
                if (b) {
                    return AjaxResultUtil.buildResponse(res,AjaxResponseCodeEnum.SUCCESS);
                }
            }
        }catch (Exception e) {
            log.error("KitchenPickController===del:{}", e.getMessage());
        }
        return AjaxResultUtil.buildResponse(res,AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }


    // 构建错误响应
    private String buildErrorResponse(JSONObject res, AjaxResponseCodeEnum errorCode) {
        res.put("msg", errorCode.getMessage());
        return AjaxResultUtil.response(res, errorCode.getCode());
    }
}
