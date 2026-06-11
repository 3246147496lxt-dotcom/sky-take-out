package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     * @return
     */
    OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单支付
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    /**
     * 根据id查询订单明细表
     * @param orderDetailId
     * @return
     */
    OrderVO orderDetailGetById(Long orderDetailId);

    /**
     * 取消订单
     * @param id
     */
    void cancel(Long id);

    /**
     * 历史订单查询
     *
     * @return
     */
    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单分页查询（商家端）
     * @param ordersPageQueryDTO
     * @return
     */
    PageResult ordersManage(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 商家接单
     * @param ordersConfirmDTO
     */
    void accept(OrdersConfirmDTO ordersConfirmDTO);

    /**
     * 商家拒单
     * @param ordersRejectionDTO
     */
    void refuse(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 商家取消订单
     */
    void cancelManage(OrdersCancelDTO ordersCancelDTO);

    /**
     * 商家派送订单
     * @param id
     */
    void delivery(Long id);

    /**
     * 商家完成订单
     * @param id
     */
    void complete(Long id);

    /**
     * 各个状态的订单数量统计
     * @return
     */
    OrderStatisticsVO count();

    /**
     * 用户催单
     * @param id
     */
    void reminder(Long id);
}
