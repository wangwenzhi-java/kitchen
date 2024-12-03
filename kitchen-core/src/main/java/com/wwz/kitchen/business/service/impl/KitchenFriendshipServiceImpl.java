package com.wwz.kitchen.business.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.dto.FriendshipRabbitDto;
import com.wwz.kitchen.business.enums.AjaxResponseCodeEnum;
import com.wwz.kitchen.business.enums.FriendshipStatusEnum;
import com.wwz.kitchen.business.enums.MessageTypeEnum;
import com.wwz.kitchen.business.service.KitchenFriendshipService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.persistence.beans.KitchenFriendship;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.persistence.mapper.KitchenFriendshipMapper;
import com.wwz.kitchen.util.AjaxResultUtil;
import com.wwz.kitchen.util.RabbitMqUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wwz.kitchen.business.enums.FriendshipStatusEnum.CONFIRMED;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-21
 */
@Service
public class KitchenFriendshipServiceImpl extends ServiceImpl<KitchenFriendshipMapper, KitchenFriendship> implements KitchenFriendshipService {
    @Autowired
    public KitchenUsersService kitchenUsersService;

    @Autowired
    private RabbitMqUtil rabbitMqUtil;
    /**
     * 根据uid获取好友列表 且按sort倒序、时间倒序排序
     * @param uid
     * @return
     */
    @Override
    public List<KitchenUsers> getFriend(Integer uid) {
        QueryWrapper<KitchenFriendship> kitchenFriendshipQueryWrapper = new QueryWrapper<>();
        kitchenFriendshipQueryWrapper.lambda().eq(KitchenFriendship::getUid,uid);
        kitchenFriendshipQueryWrapper.lambda().eq(KitchenFriendship::getStatus, CONFIRMED.getValue());
        kitchenFriendshipQueryWrapper.lambda().orderByDesc(KitchenFriendship::getSort);
        kitchenFriendshipQueryWrapper.lambda().orderByDesc(KitchenFriendship::getUpdateTime);
        List<KitchenFriendship> friendships = this.list(kitchenFriendshipQueryWrapper);
        if (friendships == null || friendships.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> friendIds = friendships.stream().map(KitchenFriendship::getFriendId).collect(Collectors.toList());
        List<KitchenUsers> kitchenUsers = kitchenUsersService.listByIds(friendIds);

        Map<Integer, KitchenUsers> userMap = kitchenUsers.stream()
                .collect(Collectors.toMap(KitchenUsers::getId, user -> user));
        List<KitchenUsers> sortedKitchenUsers = friendIds.stream()
                .map(userMap::get)
                .filter(Objects::nonNull) // 过滤掉可能缺失的用户数据
                .collect(Collectors.toList());
        return sortedKitchenUsers;
    }

    @Transactional  // 事务管理
    public boolean deleteFriendship(Integer uid, Integer friendId) {
        // 更新当前用户与好友的关系状态为删除
        QueryWrapper<KitchenFriendship> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(KitchenFriendship::getFriendId, friendId);
        queryWrapper.lambda().eq(KitchenFriendship::getUid,uid);
        queryWrapper.lambda().eq(KitchenFriendship::getStatus, FriendshipStatusEnum.CONFIRMED.getValue());
        KitchenFriendship kitchenFriendship = this.getOne(queryWrapper);

        if (kitchenFriendship != null) {
            kitchenFriendship.setUid(uid);
            kitchenFriendship.setFriendId(friendId);
            kitchenFriendship.setStatus(FriendshipStatusEnum.DELETE.getValue());
            boolean isUpdated = this.updateById(kitchenFriendship);
            if (!isUpdated) {
                return false;  // 如果更新失败，直接返回
            }
        }

        // 双删操作：删除对方的好友关系
        QueryWrapper<KitchenFriendship> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.lambda().eq(KitchenFriendship::getUid, friendId);
        queryWrapper1.lambda().eq(KitchenFriendship::getFriendId, uid);
        queryWrapper1.lambda().eq(KitchenFriendship::getStatus, FriendshipStatusEnum.CONFIRMED.getValue());
        KitchenFriendship kitchenFriendship1 = this.getOne(queryWrapper1);

        if (kitchenFriendship1 != null) {
            kitchenFriendship1.setStatus(FriendshipStatusEnum.DELETE.getValue());
            boolean isDeleted = this.updateById(kitchenFriendship1);
            if (isDeleted) {
                // 发送删除好友的消息到 RabbitMQ
                FriendshipRabbitDto friendshipRabbitDto = new FriendshipRabbitDto();
                friendshipRabbitDto.setSendUid(uid);
                friendshipRabbitDto.setType(MessageTypeEnum.DELETE_FRIEND.getCode());
                friendshipRabbitDto.setReceiveUid(friendId);

                rabbitMqUtil.sendToRabbit(MessageTypeEnum.DELETE_FRIEND.getCode(), friendshipRabbitDto);
                return true;
            }
        }
        return false;
    }
}
