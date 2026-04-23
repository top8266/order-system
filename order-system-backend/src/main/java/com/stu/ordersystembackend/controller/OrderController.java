package com.stu.ordersystembackend.controller;

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

    @GetMapping("/product/list")
    public List<Product> getProducts() {
        return productMapper.selectList(null);
    }

    @GetMapping("/list")
    public List<Orders> getOrders() {
        return orderMapper.selectList(null);
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
    // 批量完成订单
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

    // 批量删除订单
    @PostMapping("/batchDelete")
    public String batchDelete(@RequestBody List<Integer> ids) {
        orderMapper.deleteBatchIds(ids);
        return "success";
    }
}