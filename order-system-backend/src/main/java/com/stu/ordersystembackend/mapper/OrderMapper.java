package com.stu.ordersystembackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stu.ordersystembackend.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Orders> {
}