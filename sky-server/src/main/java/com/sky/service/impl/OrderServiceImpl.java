package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;

    /**
     * 用户下单
     *
     * @return
     */
    @Transactional
    public OrderSubmitVO orderSubmit(OrdersSubmitDTO ordersSubmitDTO) {

        //1.处理各种业务异常（地址簿为空，购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //根据userId查询购物车数据
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        if (list.get(0) == null || list.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //2.向订单表插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);

        //3.向订单细节表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();

        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //4.清空购物车
        shoppingCartMapper.deleteAllCart(userId);

        //5.封装vo对象
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    /*public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }*/

    /**
     * 订单支付（本地模拟版，绕过微信接口资质限制）
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        // 模拟微信生成的预支付数据）
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "SUCCESS");
        jsonObject.put("nonceStr", "mockNonceStr123456");
        jsonObject.put("package", "prepay_id=mock_prepay_id_789");
        jsonObject.put("signType", "RSA");
        jsonObject.put("paySign", "mockPaySignxxxxx");
        jsonObject.put("timeStamp", String.valueOf(System.currentTimeMillis() / 1000));

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        // 2. 依然组装 VO 返回给前端，让前端小程序拿到假数据不报错
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 根据id查询订单明细表
     *
     * @param orderId
     * @return
     */
    public OrderVO orderDetailGetById(Long orderId) {
        //查询订单明细表数据
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);

        //查询订单表数据
        Orders order = orderMapper.getById(orderId);

        //查询地址簿数据
        AddressBook addressBook = addressBookMapper.getById(order.getAddressBookId());
        String address = addressBook.getProvinceName() + addressBook.getCityName()
                + addressBook.getDistrictName() + addressBook.getDetail();
        order.setAddress(address);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(order, orderVO);
        orderVO.setOrderDetailList(orderDetailList);

        return orderVO;
    }

    /**
     * 取消订单
     *
     * @param id
     */
    public void cancel(Long id) {
        //查询订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        //如果订单状态为：已接单、派送中、已完成则不能取消，抛出异常
        Integer status = orders.getStatus();
        if (status == Orders.CONFIRMED || status == Orders.DELIVERY_IN_PROGRESS || status == Orders.COMPLETED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //修改订单表数据：已取消
        orders.setStatus(Orders.CANCELLED);
        orderMapper.update(orders);
    }

    /**
     * 历史订单分页查询
     *
     * @return
     */
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 开启 PageHelper 分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        // 1. 查出当前用户的所有订单
        Long userId = BaseContext.getCurrentId();
        List<Orders> ordersList = orderMapper.historyOrders(userId);

        List<OrderVO> orderVOList = new ArrayList<>();

        // 3. 【核心】哪怕订单是空的，也得返回 PageResult，总条数为 0
        if (CollectionUtils.isEmpty(ordersList)) {
            return new PageResult(0L, orderVOList);
        }

        for (Orders orders : ordersList) {
            //2.根据订单id查询对应订单明细表
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

            //3.封装oderVO
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDetailList(orderDetailList);

            //4.将VO添加进集合中
            orderVOList.add(orderVO);
        }

        // 5. 【核心变形】强转成 Page 对象，去获取真实的“总记录数(total)”
        Page<Orders> p = (Page<Orders>) ordersList;
        long total = p.getTotal();

        // 6. 组装并返回 PageResult（总条数 + 组装好的 VO 结果集）
        return new PageResult(total, orderVOList);
    }

    /**
     * 订单管理（商家端）
     *
     * @param ordersPageQueryDTO
     * @return
     */
    public PageResult ordersManage(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 开启 PageHelper 分页
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> page = orderMapper.page(ordersPageQueryDTO);

        long total = page.getTotal();
        List<Orders> ordersList = page.getResult();

        //查询地址
        for (Orders orders : ordersList) {
            AddressBook addressBook = addressBookMapper.getById(orders.getAddressBookId());
            String address = addressBook.getProvinceName() + addressBook.getCityName()
                    + addressBook.getDistrictName() + addressBook.getDetail();
            orders.setAddress(address);
        }

        return new PageResult(total, ordersList);
    }

    /**
     * 商家接单
     */
    public void accept(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = UncanceledOrders(ordersConfirmDTO.getId());

        //修改订单状态
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }

    /**
     * 商家拒单
     * @param ordersRejectionDTO
     */
    public void refuse(OrdersRejectionDTO ordersRejectionDTO) {
        //判断当前订单是否被取消
        Orders orders = UncanceledOrders(ordersRejectionDTO.getId());

        //修改订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());

        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     */
    public void cancelManage(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = UncanceledOrders(ordersCancelDTO.getId());

        //修改订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersCancelDTO.getCancelReason());

        orderMapper.update(orders);
    }

    /**
     * 商家派送订单
     * @param id
     */
    public void delivery(Long id) {
        Orders orders = UncanceledOrders(id);

        //修改订单状态
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * 商家完成订单
     * @param id
     */
    public void complete(Long id) {
        Orders orders = orderMapper.getById(id);
        orders.setStatus(Orders.COMPLETED);
        orderMapper.update(orders);
    }

    /**
     * 各个状态的订单数量统计
     * @return
     */
    public OrderStatisticsVO count() {
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        //查找待接单数量
        orderStatisticsVO.setToBeConfirmed(orderMapper.count(OrderVO.TO_BE_CONFIRMED));

        //查找待派送数量
        orderStatisticsVO.setConfirmed(orderMapper.count(OrderVO.CONFIRMED));

        //查找派送中数量
        orderStatisticsVO.setDeliveryInProgress(orderMapper.count(OrderVO.DELIVERY_IN_PROGRESS));

        return orderStatisticsVO;
    }

    //判断当前订单是否被取消
    private Orders UncanceledOrders(Long id){
        Orders orders = orderMapper.getById(id);
        //如果已被取消，抛出异常
        if(orders.getStatus() == Orders.CANCELLED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        return orders;
    }

}
