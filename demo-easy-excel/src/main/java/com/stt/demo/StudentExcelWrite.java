package com.stt.demo;

import com.alibaba.excel.EasyExcel;

import java.util.ArrayList;
import java.util.List;

public class StudentExcelWrite {
    public static void main(String[] args) {

        // 准备数据
        List<Student> studentList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            studentList.add(new Student(i, "stt_" + i));
        }

        // 设置输出路径
        String fileName = "d:/student.xlsx";
        // 实现写操作
        EasyExcel.write(fileName, Student.class)
                .sheet("学生信息")
                .doWrite(studentList);
    }
}
