package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userShoppingCartController")
@RequestMapping("/user/shoppingCart")
@Api(tags = "购物车接口")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    public Result addShoppingCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车:{}", shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> getList(){
        log.info("查看购物车");
        List<ShoppingCart> list = shoppingCartService.getList();
        return Result.success(list);
    }

    /**
     * 删除购物车中一个商品
     */
    @PostMapping("/sub")
    public Result deleteCart(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除购物车中一个商品：{}", shoppingCartDTO);
        shoppingCartService.deleteCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 清空购物车商品
     */
    @DeleteMapping("/clean")
    public Result deleteAllCart(){
        log.info("清空购物车");
        shoppingCartService.deleteAllCart();
        return Result.success();
    }
}
