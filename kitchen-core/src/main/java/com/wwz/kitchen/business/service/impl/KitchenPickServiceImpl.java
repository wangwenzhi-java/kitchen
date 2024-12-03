package com.wwz.kitchen.business.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.dto.KitchenPickDTO;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import com.wwz.kitchen.business.enums.StatusEnum;
import com.wwz.kitchen.business.service.KitchenPickMenuService;
import com.wwz.kitchen.business.service.KitchenPickService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.framework.security.JwtTokenUtil;
import com.wwz.kitchen.persistence.beans.KitchenPick;
import com.wwz.kitchen.persistence.beans.KitchenPickMenu;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.persistence.mapper.KitchenPickMapper;
import com.wwz.kitchen.util.AjaxResultUtil;
import com.wwz.kitchen.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-19
 */
@Service
public class KitchenPickServiceImpl extends ServiceImpl<KitchenPickMapper, KitchenPick> implements KitchenPickService {

    @Autowired
    private KitchenPickMenuService kitchenPickMenuService;  // 注入 KitchenPickMenuService

    @Transactional(rollbackFor = Exception.class)
    public String addPick(KitchenPickDTO kitchenPickDTO, Integer uid, HttpServletRequest request) {
        JSONObject res = new JSONObject();
        try {
            Arrays.stream(kitchenPickDTO.getMenus()).forEach(System.out::println);
            // 创建并保存 KitchenPick
            KitchenPick kitchenPick = new KitchenPick();
            kitchenPick.setUid(uid);
            kitchenPick.setTitle(kitchenPickDTO.getTitle());
            kitchenPick.setStatus(StatusEnum.RELEASE.toString());
            kitchenPick.setType(kitchenPickDTO.getType());
            boolean save = this.save(kitchenPick);
            if (!save) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
            }
            Integer pid = kitchenPick.getId();

            // 创建并保存 KitchenPickMenu 列表
            List<KitchenPickMenu> kitchenPickMenus = new ArrayList<>();
            Arrays.stream(kitchenPickDTO.getMenus())
                    .forEach(mid -> {
                        KitchenPickMenu kitchenPickMenu = new KitchenPickMenu();
                        kitchenPickMenu.setUid(uid);
                        kitchenPickMenu.setPickId(pid);
                        kitchenPickMenu.setStatus(StatusEnum.RELEASE.toString());
                        kitchenPickMenu.setMenuId(mid);
                        kitchenPickMenus.add(kitchenPickMenu);
                    });

            boolean b = kitchenPickMenuService.saveBatch(kitchenPickMenus);
            if (b) {
                return buildErrorResponse(res, AjaxResponseCodeEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("KitchenPickService===addPick:{}");
            throw e;  // 重新抛出异常，确保事务回滚
        }
        return buildErrorResponse(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR);
    }

    // 构建错误响应
    private String buildErrorResponse(JSONObject res, AjaxResponseCodeEnum errorCode) {
        res.put("msg", errorCode.getMessage());
        return AjaxResultUtil.response(res, errorCode.getCode());
    }
}
