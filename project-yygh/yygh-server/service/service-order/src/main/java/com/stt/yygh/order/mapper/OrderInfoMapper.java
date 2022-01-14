package com.stt.yygh.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.stt.yygh.model.order.OrderInfo;
import com.stt.yygh.vo.order.OrderCountQueryVo;
import com.stt.yygh.vo.order.OrderCountVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {
    List<OrderCountVo> selectOrderCount(@Param("vo") OrderCountQueryVo orderCountQueryVo);
}
