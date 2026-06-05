package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 添加购物车数据
     * @param shoppingCart
     */
    @Insert(" insert into shopping_cart(name,image,user_id,dish_id,setmeal_id,dish_flavor,number,amount,create_time) " +
            "values(#{name},#{image},#{userId},#{dishId},#{setmealId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void addShoppingCart(ShoppingCart shoppingCart);

    /**
     * 查找购物车数据
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    /**
     * 根据id修改商品数量
     * @param cart
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateByNumber(ShoppingCart cart);
}
