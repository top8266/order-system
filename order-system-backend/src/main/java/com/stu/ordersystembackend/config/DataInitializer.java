package com.stu.ordersystembackend.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.ordersystembackend.entity.Product;
import com.stu.ordersystembackend.entity.User;
import com.stu.ordersystembackend.mapper.ProductMapper;
import com.stu.ordersystembackend.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否存在管理员账号
        LambdaQueryWrapper<User> adminWrapper = new LambdaQueryWrapper<>();
        adminWrapper.eq(User::getRole, "admin");
        Long adminCount = userMapper.selectCount(adminWrapper);

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

        // 初始化商家数据
        initSellers();
        
        // 初始化商品数据
        initProducts();
    }

    private void initSellers() {
        String[][] sellers = {
            {"seller1", "蜜雪时光", "13812345678"},
            {"seller2", "川湘菜馆", "13987654321"},
            {"seller3", "鲜果时光", "13655556666"},
            {"seller4", "沙县小吃", "13799998888"},
            {"seller5", "汉堡王", "13577778888"},
            {"seller6", "益禾堂", "13466667777"},
            {"seller7", "兰州拉面", "13355556666"},
            {"seller8", "喜茶", "13244445555"},
            {"seller9", "黄焖鸡米饭", "13133334444"},
            {"seller10", "正新鸡排", "13022223333"},
            {"seller", "闽华便利店", "15987654321"}
        };

        for (String[] seller : sellers) {
            String username = seller[0];
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, username);
            Long count = userMapper.selectCount(wrapper);
            
            if (count == 0) {
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRole("seller");
                user.setShopName(seller[1]);
                user.setPhone(seller[2]);
                user.setStatus(1);
                user.setAuditStatus(1);
                userMapper.insert(user);
                System.out.println("已创建商家: " + seller[1]);
            }
        }
    }

    private void initProducts() {
        // 清空旧的商品数据（确保数据正确）
        productMapper.delete(null);
        
        // 先获取商家ID
        LambdaQueryWrapper<User> sellerWrapper = new LambdaQueryWrapper<>();
        sellerWrapper.eq(User::getRole, "seller");
        List<User> sellers = userMapper.selectList(sellerWrapper);

        // 商品数据: 商家用户名, 商品名称, 分类, 价格, 库存
        Object[][] productsData = {
            // seller1: 蜜雪时光
            {"seller1", "珍珠奶茶", "饮料", 8, 100},
            {"seller1", "柠檬茶", "饮料", 6, 80},
            {"seller1", "芝士奶盖", "饮料", 12, 50},
            {"seller1", "芋圆烧仙草", "饮料", 15, 60},
            {"seller1", "杨枝甘露", "饮料", 18, 40},
            // seller2: 川湘菜馆
            {"seller2", "麻辣香锅", "主食", 38, 50},
            {"seller2", "水煮鱼", "主食", 48, 30},
            {"seller2", "回锅肉", "主食", 28, 60},
            {"seller2", "宫保鸡丁", "主食", 26, 60},
            {"seller2", "麻婆豆腐", "主食", 18, 80},
            // seller3: 鲜果时光
            {"seller3", "草莓酸奶", "饮料", 12, 50},
            {"seller3", "芒果汁", "饮料", 15, 40},
            {"seller3", "西瓜冰沙", "饮料", 10, 60},
            {"seller3", "鲜榨橙汁", "饮料", 8, 70},
            {"seller3", "水果拼盘", "水果", 25, 30},
            // seller4: 沙县小吃
            {"seller4", "蒸饺", "主食", 10, 100},
            {"seller4", "拌面", "主食", 8, 80},
            {"seller4", "馄饨", "主食", 12, 60},
            {"seller4", "鸡腿饭", "主食", 18, 50},
            {"seller4", "老鸭汤", "主食", 15, 40},
            // seller5: 汉堡王
            {"seller5", "牛肉汉堡", "主食", 28, 60},
            {"seller5", "薯条", "零食", 12, 80},
            {"seller5", "炸鸡", "零食", 18, 50},
            {"seller5", "可乐", "饮料", 8, 100},
            {"seller5", "冰淇淋甜筒", "零食", 6, 100},
            // seller6: 益禾堂
            {"seller6", "烤奶", "饮料", 10, 80},
            {"seller6", "芋泥波波", "饮料", 14, 60},
            {"seller6", "西瓜啵啵", "饮料", 12, 70},
            {"seller6", "四季奶青", "饮料", 11, 70},
            {"seller6", "葡萄酸奶", "饮料", 13, 50},
            // seller7: 兰州拉面
            {"seller7", "牛肉面", "主食", 18, 80},
            {"seller7", "炒面", "主食", 16, 60},
            {"seller7", "刀削面", "主食", 15, 70},
            {"seller7", "凉拌牛肉", "零食", 25, 40},
            {"seller7", "鸡蛋汤", "饮料", 8, 100},
            // seller8: 喜茶
            {"seller8", "芝芝莓莓", "饮料", 22, 50},
            {"seller8", "多肉葡萄", "饮料", 23, 45},
            {"seller8", "满杯红柚", "饮料", 20, 55},
            {"seller8", "波波奶茶", "饮料", 18, 60},
            {"seller8", "烤布蕾", "零食", 15, 40},
            // seller9: 黄焖鸡米饭
            {"seller9", "黄焖鸡", "主食", 22, 50},
            {"seller9", "黄焖排骨", "主食", 28, 40},
            {"seller9", "黄焖豆腐", "主食", 16, 60},
            {"seller9", "米饭", "主食", 3, 200},
            {"seller9", "酸梅汤", "饮料", 6, 80},
            // seller10: 正新鸡排
            {"seller10", "鸡排", "零食", 15, 80},
            {"seller10", "鸡柳", "零食", 12, 70},
            {"seller10", "薯条", "零食", 10, 90},
            {"seller10", "香肠", "零食", 6, 100},
            {"seller10", "奶茶", "饮料", 10, 60},
            // seller: 闽华便利店（日用品）
            {"seller", "矿泉水", "饮料", 2, 200},
            {"seller", "洗衣液", "日用品", 25, 50},
            {"seller", "洗洁精", "日用品", 15, 80},
            {"seller", "卫生纸", "日用品", 18, 60},
            {"seller", "牙膏", "日用品", 12, 100},
            {"seller", "洗发水", "日用品", 35, 40},
            {"seller", "沐浴露", "日用品", 30, 50},
            {"seller", "方便面", "零食", 5, 150},
            {"seller", "面包", "零食", 8, 80},
            {"seller", "牛奶", "饮料", 6, 100}
        };

        for (Object[] product : productsData) {
            String sellerUsername = (String) product[0];
            String name = (String) product[1];
            String category = (String) product[2];
            int price = (Integer) product[3];
            int stock = (Integer) product[4];

            // 查找对应的商家
            User seller = sellers.stream()
                .filter(s -> s.getUsername().equals(sellerUsername))
                .findFirst()
                .orElse(null);

            if (seller != null) {
                Integer sellerId = seller.getId();
                
                LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Product::getName, name);
                wrapper.eq(Product::getSellerId, sellerId);
                Long count = productMapper.selectCount(wrapper);
                
                if (count == 0) {
                    Product p = new Product();
                    p.setName(name);
                    p.setCategory(category);
                    p.setPrice(Double.valueOf(price));
                    p.setStock(stock);
                    p.setSellerId(sellerId);
                    productMapper.insert(p);
                    System.out.println("已创建商品: " + seller.getShopName() + " - " + name);
                }
            }
        }
    }
}
