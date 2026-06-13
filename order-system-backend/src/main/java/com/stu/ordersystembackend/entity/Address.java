package com.stu.ordersystembackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class Address {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;       // 用户ID
    private String name;          // 收货人姓名
    private String phone;         // 收货人电话
    private String province;      // 省份
    private String city;          // 城市
    private String district;      // 区/县
    private String detail;        // 详细地址
    private Integer isDefault;    // 是否默认地址 1:是 0:否
}