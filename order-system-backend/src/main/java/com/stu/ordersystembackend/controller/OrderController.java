package com.stu.ordersystembackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.ordersystembackend.entity.Orders;
import com.stu.ordersystembackend.entity.Product;
import com.stu.ordersystembackend.mapper.OrderMapper;
import com.stu.ordersystembackend.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
@CrossOrigin(origins = "*")
public class OrderController {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    // 全部商品
    @GetMapping("/product/list")
    public List<Product> getProducts() {
        return productMapper.selectList(null);
    }

    // 按分类查询商品
    @GetMapping("/product/category")
    public List<Product> getProductByCategory(@RequestParam String category, @RequestParam(required = false) Integer sellerId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategory, category);
        if (sellerId != null) {
            wrapper.eq(Product::getSellerId, sellerId);
        }
        return productMapper.selectList(wrapper);
    }

    // 按商家查询商品
    @GetMapping("/product/seller/{sellerId}")
    public List<Product> getProductsBySeller(@PathVariable Integer sellerId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, sellerId);
        return productMapper.selectList(wrapper);
    }

    // 库存预警商品
    @GetMapping("/product/warning")
    public List<Product> getWarningProduct() {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Product::getStock, 10);
        return productMapper.selectList(wrapper);
    }

    // 商品补货接口
    @PostMapping("/product/restock")
    public String restockProduct(@RequestBody Map<String,Object> params) {
        Integer productId = (Integer) params.get("id");
        Integer addStock = (Integer) params.get("stock");
        Product product = productMapper.selectById(productId);
        if(product == null){
            return "fail";
        }
        product.setStock(product.getStock() + addStock);
        productMapper.updateById(product);
        return "success";
    }

    // 新增商品接口
    @PostMapping("/product/add")
    public String addProduct(@RequestBody Product product) {
        productMapper.insert(product);
        return "success";
    }

    // 编辑商品接口
    @PostMapping("/product/update")
    public String updateProduct(@RequestBody Product product) {
        productMapper.updateById(product);
        return "success";
    }

    // 删除商品接口
    @PostMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable Integer id) {
        productMapper.deleteById(id);
        return "success";
    }

    // 批量删除商品接口
    @PostMapping("/product/batchDelete")
    public String batchDeleteProduct(@RequestBody List<Integer> ids) {
        productMapper.deleteBatchIds(ids);
        return "success";
    }

    // 根据ID查询单个商品
    @GetMapping("/product/{id}")
    public Product getProductById(@PathVariable Integer id) {
        return productMapper.selectById(id);
    }

    // 全部订单
    @GetMapping("/list")
    public List<Orders> getOrders() {
        return orderMapper.selectList(null);
    }

    // 按状态查询订单
    @GetMapping("/all")
    public List<Map<String, Object>> getOrdersByStatus(@RequestParam(required = false) String status) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Orders::getStatus, status);
        }
        wrapper.orderByDesc(Orders::getId);
        List<Orders> orders = orderMapper.selectList(wrapper);
        
        // 转换为包含产品信息的Map
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Orders order : orders) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo());
            map.put("productId", order.getProductId());
            map.put("status", order.getStatus());
            map.put("remark", order.getRemark());
            
            // 获取产品信息
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                map.put("productName", product.getName());
                map.put("productPrice", product.getPrice());
            }
            
            result.add(map);
        }
        return result;
    }

    // 按订单号更新状态
    @PostMapping("/status/update")
    public String updateOrderStatus(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");
        String status = (String) params.get("status");
        
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getOrderNo, orderNo);
        List<Orders> orders = orderMapper.selectList(wrapper);
        
        for (Orders order : orders) {
            order.setStatus(status);
            orderMapper.updateById(order);
        }
        return "success";
    }

    // 用户历史订单
    @GetMapping("/user/order")
    public List<Orders> getUserOrder() {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, 1);
        wrapper.orderByDesc(Orders::getId);
        List<Orders> orders = orderMapper.selectList(wrapper);
        
        for (Orders order : orders) {
            if (order.getOrderNo() == null || order.getOrderNo().isEmpty()) {
                order.setOrderNo("OLD-" + order.getId());
            }
        }
        return orders;
    }

    @PostMapping("/add")
    public String addOrder(@RequestBody Orders order) {
        order.setStatus("待接单");
        orderMapper.insert(order);
        return "success";
    }

    @PostMapping("/complete/{id}")
    public String completeOrder(@PathVariable Integer id) {
        Orders order = new Orders();
        order.setId(id);
        order.setStatus("已完成");
        orderMapper.updateById(order);
        return "success";
    }

    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Integer id) {
        orderMapper.deleteById(id);
        return "success";
    }

    // 批量完成
    @PostMapping("/batchComplete")
    public String batchComplete(@RequestBody List<Integer> ids) {
        for (Integer id : ids) {
            Orders order = new Orders();
            order.setId(id);
            order.setStatus("已完成");
            orderMapper.updateById(order);
        }
        return "success";
    }

    // 批量删除
    @PostMapping("/batchDelete")
    public String batchDelete(@RequestBody List<Integer> ids) {
        orderMapper.deleteBatchIds(ids);
        return "success";
    }

    // 生成唯一订单编号
    private String generateOrderNo() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + 
               String.format("%04d", (int) (Math.random() * 10000));
    }

    // 购物车批量下单
    @PostMapping("/batchAdd")
    public String batchAddOrder(@RequestBody List<Map<String, Object>> cartList) {
        String orderNo = generateOrderNo();
        
        for (Map<String, Object> cartItem : cartList) {
            Integer productId = (Integer) cartItem.get("productId");
            Integer quantity = (Integer) cartItem.get("quantity");
            String remark = (String) cartItem.get("remark");

            Product product = productMapper.selectById(productId);
            if (product.getStock() < quantity) {
                return "库存不足";
            }
            product.setStock(product.getStock() - quantity);
            productMapper.updateById(product);

            Orders order = new Orders();
            order.setOrderNo(orderNo);
            order.setProductId(productId);
            order.setUserId(1);
            order.setStatus("待接单");  // 已支付，等待接单
            order.setRemark(remark);
            orderMapper.insert(order);
        }
        return "success";
    }
}