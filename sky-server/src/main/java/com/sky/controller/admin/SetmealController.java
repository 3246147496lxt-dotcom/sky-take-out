package com.sky.controller.admin;

import com.sky.dto.DishPageQueryDTO;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     */
    @PostMapping
    public Result saveWithDish(@RequestBody SetmealDTO setmealDTO){
        log.info("新增套餐:{}", setmealDTO);
        setmealService.saveWithDish(setmealDTO);
        return Result.success();
    }

    /**
     * 分页查询
     */
    @GetMapping("/page")
    public Result page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("开始分页查询:{}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 根据id查询套餐
     */
    @GetMapping("/{id}")
    public Result<SetmealVO> getSetmealById(@PathVariable Long id){
        log.info("根据id查询套擦:{}", id);
        SetmealVO setmealVO = setmealService.getByIdWithDish(id);
        return Result.success(setmealVO);
    }

    /**
     * 批量删除套餐
     */
    @DeleteMapping
    public Result deleteBatch(@RequestParam List<Long> ids){
        log.info("批量删除套餐:{}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改套餐
     */
    @PutMapping
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐:{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 套餐的起售、停售
     */
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, @RequestParam Long id){
        log.info("套餐的起售、停售状态：{}，id :{}",status,id);
        setmealService.startOrStop(status,id);
        return Result.success();
    }
}
