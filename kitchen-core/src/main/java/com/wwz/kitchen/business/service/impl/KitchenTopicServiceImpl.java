package com.wwz.kitchen.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.dto.KitchenTopicDTO;
import com.wwz.kitchen.business.enums.StatusEnum;
import com.wwz.kitchen.business.service.KitchenTopicService;
import com.wwz.kitchen.business.service.KitchenUsersService;
import com.wwz.kitchen.persistence.beans.KitchenTopic;
import com.wwz.kitchen.persistence.beans.KitchenUsers;
import com.wwz.kitchen.persistence.mapper.KitchenTopicMapper;
import com.wwz.kitchen.persistence.mapper.KitchenUsersMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-12-03
 */
@Service
public class KitchenTopicServiceImpl extends ServiceImpl<KitchenTopicMapper, KitchenTopic> implements KitchenTopicService {

    @Autowired
    private KitchenTopicMapper kitchenTopicMapper;
    @Autowired
    private KitchenUsersMapper kitchenUsersMapper;
    @Override
    public Integer pubTopic(KitchenTopicDTO kitchenTopicDTO, Integer uid) {
        KitchenTopic kitchenTopic = new KitchenTopic();
        kitchenTopic.setUid(uid);
        kitchenTopic.setTopicText(kitchenTopicDTO.getTopicText());
        kitchenTopic.setTopicMedia(kitchenTopicDTO.getTopicMedia());
        kitchenTopic.setTopicType(kitchenTopicDTO.getTopicType());
        kitchenTopic.setUsername(kitchenTopicDTO.getUsername());
        kitchenTopic.setStatus(StatusEnum.RELEASE.toString());
        kitchenTopic.setCreateTime(new Date());
        int insert = kitchenTopicMapper.insert(kitchenTopic);
        return insert;
    }

    @Override
    public KitchenTopicDTO getTopicByTopicId(Integer topicId) {
        KitchenTopic kitchenTopic = kitchenTopicMapper.selectById(topicId);
        KitchenUsers kitchenUsers = kitchenUsersMapper.selectById(kitchenTopic.getUid());
        KitchenTopicDTO kitchenTopicDTO = new KitchenTopicDTO();
        kitchenTopicDTO.setId(kitchenTopic.getId());
        kitchenTopicDTO.setTopicText(kitchenTopic.getTopicText());
        kitchenTopicDTO.setTopicMedia(kitchenTopic.getTopicMedia());
        kitchenTopicDTO.setTopicType(kitchenTopic.getTopicType());
        kitchenTopicDTO.setUsername(kitchenUsers.getUsername());
       // kitchenTopicDTO.set
        return null;
    }
}
