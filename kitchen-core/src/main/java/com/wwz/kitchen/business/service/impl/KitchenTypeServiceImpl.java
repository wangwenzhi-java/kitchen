package com.wwz.kitchen.business.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wwz.kitchen.business.service.KitchenTypeService;
import com.wwz.kitchen.persistence.beans.KitchenType;
import com.wwz.kitchen.persistence.mapper.KitchenTypeMapper;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author wenzhi.wang
 * @since 2024-11-14
 */
@Service
public class KitchenTypeServiceImpl extends ServiceImpl<KitchenTypeMapper, KitchenType> implements KitchenTypeService {

}
