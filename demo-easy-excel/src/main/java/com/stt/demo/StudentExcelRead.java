package com.stt.demo;

import com.alibaba.excel.EasyExcel;

public class StudentExcelRead {
    public static void main(String[] args) {
        String fileName = "d:/student.xlsx";
        EasyExcel.read(fileName, Student.class, new ExcelEventListener())
                .sheet()
                .doRead();
    }
}
