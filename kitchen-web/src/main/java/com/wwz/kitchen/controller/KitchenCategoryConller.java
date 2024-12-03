package com.wwz.kitchen.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wwz.kitchen.business.dto.KitchenCategoryDTO;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import com.wwz.kitchen.business.enums.PlatformEnum;
import com.wwz.kitchen.business.service.KitchenCategoryService;
import com.wwz.kitchen.framework.annotation.KitchenLogs;
import com.wwz.kitchen.persistence.beans.KitchenCategory;
import com.wwz.kitchen.util.AjaxResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by wenzhi.wang.
 * on 2024/11/18.
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class KitchenCategoryConller {

   @Autowired
   private KitchenCategoryService kitchenCategoryService;

   @GetMapping("/loadCategoryByType/{tid}")
   @CrossOrigin(origins = "http://localhost:8080", methods = {POST,GET},allowCredentials = "true")
   @KitchenLogs(value = "根据大类获取分类列表",platform = PlatformEnum.KITCHEN)
   public String loadCategoryByType(@PathVariable Integer tid) {
      JSONObject res = new JSONObject();
      try {
         List<KitchenCategory> categorys = kitchenCategoryService.listByTid(tid);
         List<KitchenCategoryDTO> list = categorys.stream()
                 .map(category -> {
                    // 根据 KitchenCategory 创建 KitchenCategoryDTO 对象
                    KitchenCategoryDTO dto = new KitchenCategoryDTO();
                    dto.setId(category.getId());
                    dto.setTitle(category.getTitle());
                    return dto;
                 })
                 .collect(Collectors.toList());
         res.put("categorys", list);
         res.put("msg", AjaxResponseCodeEnum.SUCCESS.getMessage());
         return AjaxResultUtil.response(res, AjaxResponseCodeEnum.SUCCESS.getCode());
      } catch (Exception e) {
         log.error("KitchenCategoryConller===loadCategoryByType:{}", e.getMessage());
      }
      res.put("msg", AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getMessage());
      return AjaxResultUtil.response(res, AjaxResponseCodeEnum.INTERNAL_SERVER_ERROR.getCode());
   }

}
