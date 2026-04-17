package com.stu.ordersystembackend.controller;

import com.stu.ordersystembackend.entity.Orders;
import com.stu.ordersystembackend.entity.Product;
import com.stu.ordersystembackend.mapper.OrderMapper;
import com.stu.ordersystembackend.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;

    // 1. 新增订单（买家端调用）
    @PostMapping("/add")
    public String addOrder(@RequestBody Orders orders) {
        orders.setStatus("待接单");
        orderMapper.insert(orders);
        return "下单成功";
    }

    // 2. 查询所有订单（卖家端调用）
    @GetMapping("/list")
    public List<Orders> getOrderList() {
        return orderMapper.selectList(null);
    }

    // 3. 修改订单状态（卖家端调用）
    @PostMapping("/update")
    public String updateOrderStatus(@RequestBody Orders orders) {
        orderMapper.updateById(orders);
        return "状态修改成功";
    }

    // 4. 删除订单（卖家端调用）
    @DeleteMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Integer id) {
        orderMapper.deleteById(id);
        return "删除成功";
    }

    // 5. 查询所有商品（买家端商品列表用）
    @GetMapping("/product/list")
    public List<Product> getProductList() {
        return productMapper.selectList(null);
    }
}