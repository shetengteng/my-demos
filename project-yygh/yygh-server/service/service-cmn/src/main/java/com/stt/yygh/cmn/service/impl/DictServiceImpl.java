package com.stt.yygh.cmn.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.stt.yygh.cmn.listener.DictListener;
import com.stt.yygh.cmn.mapper.DictMapper;
import com.stt.yygh.cmn.service.DictService;
import com.stt.yygh.model.cmn.Dict;
import com.stt.yygh.vo.cmn.DictExcelVo;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    //根据数据id查询子数据列表
    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    @Override
    public List<Dict> findChildData(Long id) {
        QueryWrapper<Dict> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", id);
        List<Dict> re = baseMapper.selectList(queryWrapper);
        //向list集合每个dict对象中设置hasChildren
        for (Dict dict : re) {
            Long dictId = dict.getId();
            boolean isChildren = this.isChildren(dictId);
            dict.setHasChildren(isChildren);
        }
        return re;
    }

    // 判断id下面是否有子节点
    private boolean isChildren(Long id) {
        QueryWrapper<Dict> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);
        Integer count = baseMapper.selectCount(wrapper);
        return count > 0;
    }

    @Override
    public void exportData(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 使用URLEncoder.encode防止中文乱码
            String fileName = URLEncoder.encode("数据字典", "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            List<Dict> dictList = baseMapper.selectList(null);
            List<DictExcelVo> dictVoList = new ArrayList<>(dictList.size());
            for (Dict dict : dictList) {
                DictExcelVo dictVo = new DictExcelVo();
                BeanUtils.copyProperties(dict, dictVo, DictExcelVo.class);
                dictVoList.add(dictVo);
            }

            EasyExcel.write(response.getOutputStream(), DictExcelVo.class)
                    .sheet("数据字典")
                    .doWrite(dictVoList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //导入数据字典
    @CacheEvict(value = "dict", allEntries = true)
    @Override
    public void importDictData(MultipartFile file) {
        try {
            EasyExcel.read(file.getInputStream(), DictExcelVo.class, new DictListener(baseMapper))
                    .sheet()
                    .doRead();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Cacheable(value = "dict", keyGenerator = "keyGenerator")
    @Override
    public String getNameByParentDictCodeAndValue(String parentDictCode, String value) {
        //如果value能唯一定位数据字典，parentDictCode可以传空，例如：省市区的value值能够唯一确定
        if (StringUtils.isEmpty(parentDictCode)) {
            Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("value", value));
            if (Objects.nonNull(dict)) {
                return dict.getName();
            }
        } else {
            Dict parentDict = this.getByDictsCode(parentDictCode);
            if (Objects.isNull(parentDict)) {
                return "";
            }
            QueryWrapper<Dict> queryWrapper = new QueryWrapper<Dict>()
                    .eq("parent_id", parentDict.getId())
                    .eq("value", value);
            Dict dict = baseMapper.selectOne(queryWrapper);
            if (Objects.nonNull(dict)) {
                return dict.getName();
            }
        }
        return "";
    }


    private Dict getByDictsCode(String parentDictCode) {
        Dict dict = baseMapper.selectOne(new QueryWrapper<Dict>().eq("dict_code", parentDictCode));
        return dict;
    }

    @Override
    public List<Dict> findByDictCode(String dictCode) {
        Dict parentDict = this.getByDictsCode(dictCode);
        List<Dict> childData = this.findChildData(parentDict.getId());
        return childData;
    }
}