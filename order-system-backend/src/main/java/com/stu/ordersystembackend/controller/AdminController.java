package com.stu.ordersystembackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.ordersystembackend.entity.Product;
import com.stu.ordersystembackend.entity.User;
import com.stu.ordersystembackend.mapper.ProductMapper;
import com.stu.ordersystembackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ProductMapper productMapper;

    // 获取所有商家
    @GetMapping("/sellers")
    public List<User> getSellers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "seller");
        List<User> users = userMapper.selectList(wrapper);
        // 确保status有值
        for (User user : users) {
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
        }
        return users;
    }

    // 获取所有配送员
    @GetMapping("/delivery")
    public List<User> getDelivery() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "delivery");
        List<User> users = userMapper.selectList(wrapper);
        for (User user : users) {
            if (user.getStatus() == null) {
                user.setStatus(1);
            }
        }
        return users;
    }

    // 新增商家/配送员
    @PostMapping("/seller/add")
    public String addSeller(@RequestBody User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return "用户名不能为空";
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "密码不能为空";
        }
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return "用户名已存在";
        }
        user.setStatus(1);  // 默认启用
        userMapper.insert(user);
        // 记录日志
        addLog("新增", "新增用户: " + user.getUsername() + " (角色: " + user.getRole() + ")");
        return "success";
    }

    // 更改用户状态（启用/禁用）
    @PostMapping("/seller/status")
    public String updateSellerStatus(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer status = (Integer) params.get("status");
        User user = userMapper.selectById(id);
        if (user == null) {
            return "用户不存在";
        }
        user.setStatus(status);
        userMapper.updateById(user);
        addLog("修改", "更改用户状态: " + user.getUsername() + " -> " + (status == 1 ? "启用" : "禁用"));
        return "success";
    }

    // 删除用户
    @PostMapping("/seller/delete/{id}")
    public String deleteSeller(@PathVariable Integer id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            return "用户不存在";
        }
        userMapper.deleteById(id);
        addLog("删除", "删除用户: " + user.getUsername());
        return "success";
    }

    // 审核商家/配送员
    @PostMapping("/seller/audit")
    public String auditSeller(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer auditStatus = (Integer) params.get("auditStatus");  // 1:通过 2:拒绝
        User user = userMapper.selectById(id);
        if (user == null) {
            return "用户不存在";
        }
        user.setAuditStatus(auditStatus);
        userMapper.updateById(user);
        addLog("审核", "审核用户: " + user.getUsername() + " -> " + (auditStatus == 1 ? "通过" : "拒绝"));
        return "success";
    }

    // 更新用户信息
    @PostMapping("/seller/update")
    public String updateSeller(@RequestBody User user) {
        Integer id = user.getId();
        if (id == null) {
            return "用户ID不能为空";
        }
        User existingUser = userMapper.selectById(id);
        if (existingUser == null) {
            return "用户不存在";
        }
        
        // 更新可修改的字段
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getShopName() != null) {
            existingUser.setShopName(user.getShopName());
        }
        if (user.getRealName() != null) {
            existingUser.setRealName(user.getRealName());
        }
        
        userMapper.updateById(existingUser);
        addLog("修改", "修改用户信息: " + existingUser.getUsername());
        return "success";
    }
    
    // 获取待审核用户列表
    @GetMapping("/pending")
    public List<User> getPendingUsers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getRole, "seller", "delivery");
        wrapper.eq(User::getAuditStatus, 0);  // 待审核
        return userMapper.selectList(wrapper);
    }

    // 操作日志存储（内存存储，简单实现）
    private static List<Map<String, Object>> logs = new ArrayList<>();

    // 获取操作日志
    @GetMapping("/logs")
    public List<Map<String, Object>> getLogs() {
        // 返回最近50条日志
        if (logs.size() > 50) {
            return logs.subList(logs.size() - 50, logs.size());
        }
        return logs;
    }

    // 添加日志
    private void addLog(String type, String content) {
        Map<String, Object> log = new HashMap<>();
        log.put("id", logs.size() + 1);
        log.put("type", type);
        log.put("content", content);
        log.put("time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        logs.add(log);
    }
    
    // 批量分配商品给商家
    @PostMapping("/product/assign")
    public String assignProductsToSeller(@RequestBody Map<String, Object> params) {
        Integer sellerId = (Integer) params.get("sellerId");
        @SuppressWarnings("unchecked")
        List<Integer> productIds = (List<Integer>) params.get("productIds");
        
        if (sellerId == null) {
            return "商家ID不能为空";
        }
        if (productIds == null || productIds.isEmpty()) {
            return "请选择要分配的商品";
        }
        
        for (Integer productId : productIds) {
            Product product = productMapper.selectById(productId);
            if (product != null) {
                product.setSellerId(sellerId);
                productMapper.updateById(product);
            }
        }
        
        User seller = userMapper.selectById(sellerId);
        addLog("分配", "分配" + productIds.size() + "件商品给商家: " + (seller != null ? seller.getUsername() : "未知"));
        return "success";
    }
    
    // 获取未分配商家的商品列表
    @GetMapping("/product/unassigned")
    public List<Product> getUnassignedProducts() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(Product::getSellerId);
        return productMapper.selectList(wrapper);
    }
}