package com.stu.ordersystembackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.ordersystembackend.entity.User;
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

    // 获取所有商家和配送员
    @GetMapping("/sellers")
    public List<User> getSellers() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(User::getRole, "seller", "delivery");
        List<User> users = userMapper.selectList(wrapper);
        // 确保status有值
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
}