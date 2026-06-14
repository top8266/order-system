package com.stu.ordersystembackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.ordersystembackend.entity.User;
import com.stu.ordersystembackend.mapper.UserMapper;
import com.stu.ordersystembackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 用户注册
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }
        
        // 密码加密
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认状态
        user.setStatus(1);
        
        // 根据角色设置审核状态
        if (user.getRole() == null) {
            user.setRole("buyer");  // 默认为买家
        }
        
        // 商家和配送员需要审核
        if ("seller".equals(user.getRole()) || "delivery".equals(user.getRole())) {
            user.setAuditStatus(0);  // 待审核
        } else {
            user.setAuditStatus(1);  // 买家直接通过
        }
        
        userMapper.insert(user);
        
        result.put("success", true);
        result.put("message", "注册成功");
        if ("seller".equals(user.getRole()) || "delivery".equals(user.getRole())) {
            result.put("message", "注册成功，请等待管理员审核");
        }
        return result;
    }

    // 用户登录
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        
        String username = params.get("username");
        String password = params.get("password");
        
        // 查找用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "密码错误");
            return result;
        }
        
        // 检查账户状态
        if (user.getStatus() == 0) {
            result.put("success", false);
            result.put("message", "账户已被禁用");
            return result;
        }
        
        // 检查审核状态（商家和配送员）
        if ("seller".equals(user.getRole()) || "delivery".equals(user.getRole())) {
            if (user.getAuditStatus() == 0) {
                result.put("success", false);
                result.put("message", "账户待审核，请等待管理员审核通过");
                return result;
            }
            if (user.getAuditStatus() == 2) {
                result.put("success", false);
                result.put("message", "账户审核被拒绝，请联系管理员");
                return result;
            }
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", getUserInfo(user));
        return result;
    }

    // 获取用户信息
    private Map<String, Object> getUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("role", user.getRole());
        info.put("status", user.getStatus());
        info.put("auditStatus", user.getAuditStatus());
        info.put("phone", user.getPhone());
        info.put("avatar", user.getAvatar());
        info.put("shopName", user.getShopName());
        info.put("shopAddress", user.getShopAddress());
        info.put("shopNotice", user.getShopNotice());
        info.put("defaultAddress", user.getDefaultAddress());
        info.put("realName", user.getRealName());
        return info;
    }

    // 获取当前用户信息
    @GetMapping("/info")
    public Map<String, Object> getUserInfo(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || !jwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "未登录或Token无效");
            return result;
        }
        
        Integer userId = jwtUtil.getUserId(token);
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        result.put("success", true);
        result.put("user", getUserInfo(user));
        return result;
    }

    // 更新用户信息
    @PostMapping("/update")
    public Map<String, Object> updateUser(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody User updateUser) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || !jwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "未登录或Token无效");
            return result;
        }
        
        Integer userId = jwtUtil.getUserId(token);
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        // 更新允许修改的字段
        if (updateUser.getPhone() != null) user.setPhone(updateUser.getPhone());
        if (updateUser.getAvatar() != null) user.setAvatar(updateUser.getAvatar());
        if (updateUser.getShopName() != null) user.setShopName(updateUser.getShopName());
        if (updateUser.getShopNotice() != null) user.setShopNotice(updateUser.getShopNotice());
        if (updateUser.getDefaultAddress() != null) user.setDefaultAddress(updateUser.getDefaultAddress());
        if (updateUser.getRealName() != null) user.setRealName(updateUser.getRealName());
        
        userMapper.updateById(user);
        
        result.put("success", true);
        result.put("message", "更新成功");
        result.put("user", getUserInfo(user));
        return result;
    }

    // 修改密码
    @PostMapping("/changePassword")
    public Map<String, Object> changePassword(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Map<String, String> params) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || !jwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "未登录或Token无效");
            return result;
        }
        
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        Integer userId = jwtUtil.getUserId(token);
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            result.put("success", false);
            result.put("message", "原密码错误");
            return result;
        }
        
        // 更新新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        
        result.put("success", true);
        result.put("message", "密码修改成功");
        return result;
    }
}