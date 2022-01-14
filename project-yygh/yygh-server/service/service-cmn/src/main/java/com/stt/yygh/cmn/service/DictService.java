package com.stt.yygh.cmn.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.stt.yygh.model.cmn.Dict;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DictService extends IService<Dict> {
    //根据数据id查询子数据列表
    List<Dict> findChildData(Long id);

    // 导出
    void exportData(HttpServletResponse response);

    // 导入
    void importDictData(MultipartFile file);

    String getNameByParentDictCodeAndValue(String parentDictCode, String value);

    List<Dict> findByDictCode(String dictCode);
}