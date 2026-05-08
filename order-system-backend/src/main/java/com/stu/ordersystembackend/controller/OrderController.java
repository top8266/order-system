package com.stu.ordersystembackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stu.ordersystembackend.entity.Orders;
import com.stu.ordersystembackend.entity.Product;
import com.stu.ordersystembackend.mapper.OrderMapper;
import com.stu.ordersystembackend.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public List<Product> getProductByCategory(@RequestParam String category) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategory, category);
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

    // 全部订单
    @GetMapping("/list")
    public List<Orders> getOrders() {
        return orderMapper.selectList(null);
    }

    // 用户历史订单
    @GetMapping("/user/order")
    public List<Orders> getUserOrder() {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, 1);
        return orderMapper.selectList(wrapper);
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

    // 购物车批量下单
    @PostMapping("/batchAdd")
    public String batchAddOrder(@RequestBody List<Map<String, Object>> cartList) {
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
            order.setProductId(productId);
            order.setUserId(1);
            order.setStatus("待接单");
            order.setRemark(remark);
            orderMapper.insert(order);
        }
        return "success";
    }
}