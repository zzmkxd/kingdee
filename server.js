const express = require("express");
const path = require("path");
const serveIndex = require("serve-index");

const app = express();
const PORT = 3003;

// 允许跨域的白名单
const allowOrigins = [
    "http://127.0.0.1:8881",
    "http://127.0.0.1:3003",
    "http://10.26.0.2:8072",
];

app.all("*", function (req, res, next) {
    const origin = req.headers.origin;
    if (allowOrigins.includes(origin)) {
        res.header("Access-Control-Allow-Origin", origin);
    }
    res.header("Access-Control-Allow-Credentials", true);
    res.header(
        "Access-Control-Allow-Headers",
        "Content-Type,Content-Length,Authorization,Accept,X-Requested-With"
    );
    res.header("Access-Control-Allow-Methods", "PUT,POST,GET,DELETE,OPTIONS");
    next();
});

// 静态资源
app.use("/", express.static(path.join(__dirname, "public")));

// 目录索引
app.use("/", serveIndex(path.join(__dirname, "public"), { icons: true }));

// 启动
app.listen(PORT, () => {
    console.log(`CustomControl Static Server is running on http://localhost:${PORT}`);
});