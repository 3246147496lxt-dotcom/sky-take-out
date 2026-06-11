package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     * @param number
     */
    @Select("select * from orders where number = #{number}")
    Orders getByNumber(String number);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    /**
     * 根据ID查询订单
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 根据id删除订单
     * @param id
     */
    @Delete("delete from orders where id = #{id}")
    void delete(String id);

    /**
     * 查询历史订单
     * @return
     */
    @Select("select * from orders where user_id = #{userId} order by order_time desc")
    List<Orders> historyOrders(Long userId);

    /**
     * 分页查询
     * @param ordersPageQueryDTO
     * @return
     */
    Page<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据status统计订单数量
     *
     * @param status
     * @return
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer count(Integer status);

    /**
     * 处理超时订单
     * @param status
     * @param time
     */
    @Select("select * from orders where status = #{status} and order_time < #{time} ")
    List<Orders> getByStatusAndOrderTimeLT(Integer status, LocalDateTime time);

    /**
     * 统计营业额
     * @return
     */
    Double sumByMap(Map map);
}
