package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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
}
