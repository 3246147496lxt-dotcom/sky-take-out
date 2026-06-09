package com.sky.controller.admin;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "订单接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 订单分页查询
     */
    @GetMapping("/conditionSearch")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("分页查询", ordersPageQueryDTO);
        PageResult pageResult = orderService.ordersManage(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/details/{id}")
    public Result<OrderVO> getById(@PathVariable Long id){
        log.info("查询订单详情:{}", id);
        OrderVO orderVO = orderService.orderDetailGetById(id);
        return Result.success(orderVO);
    }

    /**
     * 接单
     */
    @PutMapping("/confirm")
    public Result accept(@RequestBody OrdersConfirmDTO ordersConfirmDTO){
        log.info("商家接单：{}", ordersConfirmDTO);
        orderService.accept(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 拒单
     */
    @PutMapping("/rejection")
    public Result refuse(@RequestBody OrdersRejectionDTO ordersRejectionDTO){
        log.info("商家拒单：{}", ordersRejectionDTO);
        orderService.refuse(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO){
        log.info("商家取消订单:{}", ordersCancelDTO);
        orderService.cancelManage(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 商家派送订单
     */
    @PutMapping("/delivery/{id}")
    public Result delivery(@PathVariable Long id){
        log.info("商家派送订单:{}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 商家完成订单
     */
    @PutMapping("/complete/{id}")
    public Result complete(@PathVariable Long id){
        log.info("商家完成订单:{}", id);
        orderService.complete(id);
        return Result.success();
    }

    /**
     * 各个状态的订单数量统计
     */
    @GetMapping("/statistics")
    public Result count(){
        log.info("各个状态的订单数量统计:{}");
        OrderStatisticsVO orderStatisticsVO = orderService.count();
        return Result.success(orderStatisticsVO);
    }

}
