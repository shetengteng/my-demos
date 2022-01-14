// 引入 http 模块，使用es5的语法
const http = require('http')
// 创建服务器
http.createServer(function (request, response) {
    // 发送HTTP头部
    // HTTP 状态值: 200 : OK
    // 内容类型: text/plain
    response.writeHead(200,{'Content-Type':'text/html'})
    // 发送响应的数据
    response.end('<h1>Hello World</h1>')
}).listen(9527)
// 启动完成后打印
console.log('Server running at http://127.0.0.1:9527/')