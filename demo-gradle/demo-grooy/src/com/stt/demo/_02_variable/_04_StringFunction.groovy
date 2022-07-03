package com.stt.demo._02_variable

// 字符串方法讲解
def str = "groovy"
// 填充，将字符串扩展到8个字符，不够的，使用 指定字符填充，center表示原始字符串处于中间位置
println str.center(8, 'a')
// 如果没有指定填充的字符串，使用空格填充
println str.center(8)

// 在字符串的左边和右边填充
println str.padLeft(8, 'a')
println str.padRight(8, 'a')

// 字符串的比较
def str1 = 'a'
def str2 = 'b'
println str1.compareTo(str2)
// 使用比较操作符
println str1 > str2

// 通过索引获取指定下标的值
def str3 = "hello"
println str3[0]
println str3[0..1]

// 字符串减法操作
def str4 = "hello"
def str5 = "he"
println str4.minus(str5)
// 使用减法操作符效果一样
println str4 - str5

// 倒序
def str6 = "groovy"
println str6.reverse()

// 首字母大写
def str7 = "groovy"
println str7.capitalize()

// 判断是否是数字
def str8 = "88"
println str8.isNumber()