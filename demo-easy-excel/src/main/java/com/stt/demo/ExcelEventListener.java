package com.stt.demo;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.Map;

public class ExcelEventListener extends AnalysisEventListener<Student> {

    // 读取表头
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        // 读取表头信息
        System.out.println("表头信息: " + headMap);
    }

    // 一行一行的读取excel内容，从第二行开始读取，第一行是列名称
    @Override
    public void invoke(Student student, AnalysisContext analysisContext) {
        System.out.println(student);
    }

    // 读取之后进行处理
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
