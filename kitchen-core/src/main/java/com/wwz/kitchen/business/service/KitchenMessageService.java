package com.wwz.kitchen.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wwz.kitchen.business.dto.ChatMessageDTO;
import com.wwz.kitchen.persistence.beans.KitchenMessage;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-21
 */
public interface KitchenMessageService extends IService<KitchenMessage> {


    Page<ChatMessageDTO> getChatHistoryMessage(Integer uid, Integer friendId, Integer lastMessageId, int size);

    Integer getUnreadMessagesByFriendId(Integer uid, Integer friendId);

    boolean readMessages(Integer senderId, Integer receiverId);
}
