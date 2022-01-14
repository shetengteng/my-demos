// 这里使用的是CommonJS模块化方式，这种方式不支持ES6的语法，所以不需要Babel转码
exports.info = function (str) {
    document.write(str)
}
