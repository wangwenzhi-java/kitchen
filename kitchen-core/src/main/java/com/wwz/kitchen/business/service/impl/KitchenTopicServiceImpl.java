package com.wwz.kitchen.business.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.service.KitchenTopicService;
import com.wwz.kitchen.persistence.beans.KitchenTopic;
import com.wwz.kitchen.persistence.mapper.KitchenTopicMapper;
import org.springframework.stereotype.Service;

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

}
