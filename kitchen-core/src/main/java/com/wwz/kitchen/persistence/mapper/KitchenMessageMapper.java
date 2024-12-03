package com.wwz.kitchen.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wwz.kitchen.persistence.beans.KitchenMessage;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-21
 */
public interface KitchenMessageMapper extends BaseMapper<KitchenMessage> {

    //此方法在企业级应用中 大量数据存在，需要在 kitchen_message 表的 sender_id 和 receiver_id 列上创建一个复合索引来 提高效率
    //CREATE INDEX idx_sender_receiver ON kitchen_message(sender_id, receiver_id);
    @Update("UPDATE kitchen_message SET is_read = 1 WHERE sender_id = #{senderId} AND receiver_id = #{receiverId}")
    boolean readMessages(Integer senderId, Integer receiverId);
}
