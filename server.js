const express = require("express");
const path = require("path");
const serveIndex = require("serve-index");

const app = express();
const PORT = 3002;

// 设置跨域访问
app.all('*', function (req, res, next) {
    // 设置可以跨域访问的域名，比如 http://localhost:8881
    res.header('Access-Control-Allow-Origin', 'http://127.0.0.1:8881');
    res.header('Access-Control-Allow-Credentials', true);
    res.header('Access-Control-Allow-Headers', 'Content-Type,Content-Length, Authorization, Accept,X-Requested-With');
    res.header('Access-Control-Allow-Methods', 'PUT,POST,GET,DELETE,OPTIONS');
    // res.header('Content-Type','text/css;test/javascript;text/html;application/json;application/x-img');
    next();
});

// 设置静态资源文件夹
app.use('/',express.static(path.join(__dirname, 'public')));

// 设置静态资源索引
app.use('/', serveIndex(path.join(__dirname, 'public'), {'icons': true}));

// 启动服务器
app.listen(PORT, () => {
    console.log(`CustomControl Static Server is Running on http://localhost:$ {PORT}`);
});
