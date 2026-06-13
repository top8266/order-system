package com.stu.ordersystembackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stu.ordersystembackend.entity.Address;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressMapper extends BaseMapper<Address> {
}