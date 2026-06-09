package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.OrderDetail;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api(tags = "订单接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    public Result<OrderSubmitVO> orderSubmit(@RequestBody OrdersSubmitDTO ordersSubmitDTO){
        log.info("用户下单:{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.orderSubmit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * 本地模拟支付成功回调（专供个人开发测试，免内网穿透，免证书解密）
     * 路径改成了 /paySuccess/mock
     */
    @PostMapping("/paySuccess/mock")
    public void paySuccessNotifyMock(@RequestBody Map<String, String> body) throws Exception {
        // 1. 直接从前端传过来的 JSON 里拿商户订单号（不经过微信，不需要解密）
        String outTradeNo = body.get("out_trade_no");

        log.info("【本地模拟回调】收到订单支付成功通知，商户平台订单号：{}", outTradeNo);

        // 2. 直接执行核心业务处理：修改订单状态、微信小程序/商家端来单提醒
        if (outTradeNo != null) {
            orderService.paySuccess(outTradeNo);
            log.info("【本地模拟回调】订单 {} 状态修改成功，已触发后续业务！", outTradeNo);
        }
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/orderDetail/{orderId}")
    public Result<OrderVO> orderDetailGetById(@PathVariable Long orderId){
        log.info("订单明细表id:{}", orderId);
        OrderVO orderVO = orderService.orderDetailGetById(orderId);
        return Result.success(orderVO);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel/{id}")
    public Result cancel(@PathVariable Long id){
        log.info("取消订单:{}", id);
        orderService.cancel(id);
        return Result.success();
    }

    /**
     * 历史订单查询
     */
    @GetMapping("/historyOrders")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("历史订单查询");
        PageResult pageResult = orderService.historyOrders(ordersPageQueryDTO);
        return Result.success(pageResult);
    }
}
