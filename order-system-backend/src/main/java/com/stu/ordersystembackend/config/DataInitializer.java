package com.stu.ordersystembackend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.ordersystembackend.entity.User;
import com.stu.ordersystembackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否存在管理员账号
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, "admin");
        Long adminCount = userMapper.selectCount(wrapper);

        if (adminCount == 0) {
            // 创建默认管理员账号
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("admin");
            admin.setStatus(1);
            admin.setAuditStatus(1);
            userMapper.insert(admin);
            System.out.println("========================================");
            System.out.println("  默认管理员账号已创建");
            System.out.println("  用户名: admin");
            System.out.println("  密码: admin123");
            System.out.println("========================================");
        }
    }
}
