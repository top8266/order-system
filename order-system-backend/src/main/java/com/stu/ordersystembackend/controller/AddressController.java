package com.stu.ordersystembackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.ordersystembackend.entity.Address;
import com.stu.ordersystembackend.mapper.AddressMapper;
import com.stu.ordersystembackend.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/address")
@CrossOrigin(origins = "*")
public class AddressController {

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private JwtUtil jwtUtil;

    // 获取用户所有地址
    @GetMapping("/list")
    public Map<String, Object> getAddressList(@RequestHeader(value = "Authorization", required = false) String token) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || !jwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "未登录或Token无效");
            return result;
        }
        
        Integer userId = jwtUtil.getUserId(token);
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.orderByDesc(Address::getIsDefault);
        List<Address> addresses = addressMapper.selectList(wrapper);
        
        result.put("success", true);
        result.put("data", addresses);
        return result;
    }

    // 新增地址
    @PostMapping("/add")
    public Map<String, Object> addAddress(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Address address) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || !jwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "未登录或Token无效");
            return result;
        }
        
        Integer userId = jwtUtil.getUserId(token);
        address.setUserId(userId);
        
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        }
        
        addressMapper.insert(address);
        
        result.put("success", true);
        result.put("message", "添加成功");
        result.put("data", address);
        return result;
    }

    // 更新地址
    @PostMapping("/update")
    public Map<String, Object> updateAddress(
            @RequestHeader(value = "Authorization", required = false) String token,
            @RequestBody Address address) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || !jwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "未登录或Token无效");
            return result;
        }
        
        Integer userId = jwtUtil.getUserId(token);
        
        // 验证地址属于当前用户
        Address existing = addressMapper.selectById(address.getId());
        if (existing == null || !existing.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "地址不存在或无权限");
            return result;
        }
        
        // 如果设置为默认地址，先取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            cancelOtherDefault(userId);
        }
        
        addressMapper.updateById(address);
        
        result.put("success", true);
        result.put("message", "更新成功");
        result.put("data", address);
        return result;
    }

    // 删除地址
    @PostMapping("/delete/{id}")
    public Map<String, Object> deleteAddress(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || !jwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "未登录或Token无效");
            return result;
        }
        
        Integer userId = jwtUtil.getUserId(token);
        
        // 验证地址属于当前用户
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "地址不存在或无权限");
            return result;
        }
        
        addressMapper.deleteById(id);
        
        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    // 设置默认地址
    @PostMapping("/setDefault/{id}")
    public Map<String, Object> setDefaultAddress(
            @RequestHeader(value = "Authorization", required = false) String token,
            @PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        
        if (token == null || !jwtUtil.validateToken(token)) {
            result.put("success", false);
            result.put("message", "未登录或Token无效");
            return result;
        }
        
        Integer userId = jwtUtil.getUserId(token);
        
        // 验证地址属于当前用户
        Address existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "地址不存在或无权限");
            return result;
        }
        
        // 取消其他默认地址
        cancelOtherDefault(userId);
        
        // 设置当前为默认
        existing.setIsDefault(1);
        addressMapper.updateById(existing);
        
        result.put("success", true);
        result.put("message", "设置成功");
        return result;
    }

    // 取消其他默认地址
    private void cancelOtherDefault(Integer userId) {
        LambdaQueryWrapper<Address> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Address::getUserId, userId);
        wrapper.eq(Address::getIsDefault, 1);
        List<Address> defaults = addressMapper.selectList(wrapper);
        for (Address addr : defaults) {
            addr.setIsDefault(0);
            addressMapper.updateById(addr);
        }
    }
}