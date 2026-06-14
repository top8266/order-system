package com.stu.ordersystembackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String role;          // buyer/seller/delivery/admin
    private Integer status;       // 1:正常 0:禁用
    private Integer auditStatus;  // 0:待审核 1:已通过 2:已拒绝 (商家/配送员)
    private String phone;         // 电话
    private String avatar;        // 头像URL
    private String shopName;      // 商家店铺名称
    private String shopNotice;    // 商家店铺公告
    private String shopAddress;   // 商家店铺地址（用于配送员取货）
    private String defaultAddress; // 用户默认收货地址
    private String realName;      // 配送员真实姓名
}