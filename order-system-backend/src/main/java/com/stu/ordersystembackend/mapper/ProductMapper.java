package com.stu.ordersystembackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stu.ordersystembackend.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}