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

    // 全部商品（支持按商家过滤）
    @GetMapping("/product/list")
    public List<Product> getProducts(@RequestParam(required = false) Integer sellerId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (sellerId != null) {
            wrapper.eq(Product::getSellerId, sellerId);
        }
        return productMapper.selectList(wrapper);
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

    // 库存预警商品（支持按商家过滤）
    @GetMapping("/product/warning")
    public List<Product> getWarningProduct(@RequestParam(required = false) Integer sellerId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.lt(Product::getStock, 10);
        if (sellerId != null) {
            wrapper.eq(Product::getSellerId, sellerId);
        }
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

    // 全部订单（可选按商家ID过滤）
    @GetMapping("/list")
    public List<Map<String, Object>> getOrders(@RequestParam(required = false) Integer sellerId) {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        if (sellerId != null) {
            wrapper.eq(Orders::getSellerId, sellerId);
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
            map.put("quantity", order.getQuantity() != null ? order.getQuantity() : 1);
            map.put("address", order.getAddress() != null ? order.getAddress() : "未填写");
            map.put("userPhone", order.getUserPhone());
            map.put("sellerPhone", order.getSellerPhone());
            
            // 获取产品信息
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                map.put("productName", product.getName());
                map.put("productPrice", product.getPrice());
            } else {
                map.put("productName", "商品ID:" + order.getProductId() + "(已删除)");
                map.put("productPrice", 0);
            }
            
            result.add(map);
        }
        return result;
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
            } else {
                map.put("productName", "商品ID:" + order.getProductId() + "(已删除)");
                map.put("productPrice", 0);
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
    public List<Map<String, Object>> getUserOrder() {
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Orders::getUserId, 1);
        wrapper.orderByDesc(Orders::getId);
        List<Orders> orders = orderMapper.selectList(wrapper);
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Orders order : orders) {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", order.getId());
            map.put("orderNo", order.getOrderNo() != null && !order.getOrderNo().isEmpty() ? order.getOrderNo() : "OLD-" + order.getId());
            map.put("productId", order.getProductId());
            map.put("status", order.getStatus());
            map.put("remark", order.getRemark());
            map.put("quantity", order.getQuantity() != null ? order.getQuantity() : 1);  // 返回数量
            map.put("address", order.getAddress() != null ? order.getAddress() : "未填写");
            map.put("sellerPhone", order.getSellerPhone());  // 商家电话
            
            // 获取产品信息
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                map.put("productName", product.getName());
                map.put("productPrice", product.getPrice());
            } else {
                map.put("productName", "商品ID:" + order.getProductId() + "(已删除)");
                map.put("productPrice", 0);
            }
            
            result.add(map);
        }
        return result;
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

    // 购物车批量下单（按商家分组）
    @PostMapping("/batchAdd")
    public String batchAddOrder(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cartList = (List<Map<String, Object>>) request.get("cartList");
        String address = (String) request.get("address");
        String userPhone = (String) request.get("userPhone");
        
        // 按商家ID分组商品
        java.util.Map<Integer, java.util.List<Map<String, Object>>> sellerGroups = new java.util.HashMap<>();
        
        for (Map<String, Object> cartItem : cartList) {
            Integer productId = (Integer) cartItem.get("productId");
            Product product = productMapper.selectById(productId);
            if (product == null) {
                return "商品不存在";
            }
            Integer sellerId = product.getSellerId();
            
            if (!sellerGroups.containsKey(sellerId)) {
                sellerGroups.put(sellerId, new java.util.ArrayList<>());
            }
            sellerGroups.get(sellerId).add(cartItem);
        }
        
        // 为每个商家创建独立订单
        for (java.util.Map.Entry<Integer, java.util.List<Map<String, Object>>> entry : sellerGroups.entrySet()) {
            String orderNo = generateOrderNo();  // 每个商家一个订单号
            java.util.List<Map<String, Object>> items = entry.getValue();
            
            // 获取商家电话（从第一个商品获取）
            String sellerPhone = null;
            if (!items.isEmpty()) {
                Integer firstProductId = (Integer) items.get(0).get("productId");
                Product firstProduct = productMapper.selectById(firstProductId);
                if (firstProduct != null) {
                    sellerPhone = firstProduct.getSellerPhone();
                }
            }
            
            for (Map<String, Object> cartItem : items) {
                Integer productId = (Integer) cartItem.get("productId");
                Integer quantity = (Integer) cartItem.get("quantity");
                String remark = (String) cartItem.get("remark");

                Product product = productMapper.selectById(productId);
                if (product.getStock() < quantity) {
                    return "商品ID:" + productId + "库存不足";
                }
                product.setStock(product.getStock() - quantity);
                productMapper.updateById(product);

                Orders order = new Orders();
                order.setOrderNo(orderNo);
                order.setProductId(productId);
                order.setUserId(1);
                order.setSellerId(product.getSellerId());
                order.setStatus("待接单");
                order.setRemark(remark);
                order.setQuantity(quantity);
                order.setAddress(address);
                order.setUserPhone(userPhone);
                order.setSellerPhone(sellerPhone);
                orderMapper.insert(order);
            }
        }
        return "success";
    }
}