package com.stt.demo;

import com.alibaba.excel.annotation.ExcelProperty;

public class Student {

    public Student() {
    }

    public Student(Integer no, String name) {
        this.no = no;
        this.name = name;
    }

    // 设置excel列名称
    // value 用于写操作时的列名称，而index可以指定读取的列编号
    @ExcelProperty(value = "学生编号", index = 0)
    private Integer no;

    @ExcelProperty(value = "学生姓名", index = 1)
    private String name;

    public Integer getNo() {
        return no;
    }

    public void setNo(Integer no) {
        this.no = no;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
