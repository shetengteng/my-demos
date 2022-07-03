package com.stt.demo._02_variable

def name = 'stt'
println name.class

def name2 = 'a string \'s str'
println name2.class

def name3 = '''\
line 1
line 2
line 3
'''
println name3
println name3.class

def name4 = "stt"

println name4.class

// 可扩展表达式，拼接字符串
def name5 = "world"
def name6 = "hello ${name5}"
println name6
println name6.class

// 可扩展表达式，进行运算处理
def sum = "2 + 3 = ${2 + 3}"
println sum

// String与GString的相互转化
def sum2 = "2 + 3 = ${2 + 3}"
println sum2.class
String echo(String msg) {
    return msg
}

def result = echo(sum2)
println result.class