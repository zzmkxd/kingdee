function init(model, props) {
    // 生成词云
    function generateWordCloud() {
        // 创建一个文本节点用于存放所有词
        const textContainer = document.createElement('div');
        textContainer.style.lineHeight = '2';

        wordData.forEach((word, index) => {
            const wordElement = document.createElement('span');
            wordElement.className = 'word';
            wordElement.textContent = word.text;
            wordElement.style.fontSize = `${word.weight * 2 + 14}px`; // 根据权重设置字体大小
            wordElement.style.color = word.color; // 设置文字颜色

            // 添加点击事件
            wordElement.addEventListener('click', () => {
                showPopup(word.text);
                model.invoke('wordClicked', word.text);
            });

            // 添加空格
            if (index < wordData.length - 1) {
                const space = document.createTextNode(' ');
                textContainer.appendChild(wordElement);
                textContainer.appendChild(space);
            } else {
                textContainer.appendChild(wordElement);
            }
        });

        wordCloud.appendChild(textContainer);
    }

    // 根据背景色计算对比色（确保文字可读性）
    function getContrastColor(hexColor) {
        // 将十六进制颜色转换为RGB
        const r = parseInt(hexColor.substr(1, 2), 16);
        const g = parseInt(hexColor.substr(3, 2), 16);
        const b = parseInt(hexColor.substr(5, 2), 16);

        // 计算亮度
        const brightness = (r * 299 + g * 587 + b * 114) / 1000;

        // 根据亮度返回黑色或白色
        return brightness > 128 ? '#000000' : '#FFFFFF';
    }

    // 随机排列词云中的词的顺序
    function randomizeWordPositions() {
        // 随机打乱wordData数组
        for (let i = wordData.length - 1; i > 0; i--) {
            const j = Math.floor(Math.random() * (i + 1));
            [wordData[i], wordData[j]] = [wordData[j], wordData[i]];
        }

        // 清空词云容器
        wordCloud.innerHTML = '';

        // 重新生成词云
        generateWordCloud();
    }

    console.log("wordcloud inited")
    // 页面加载时生成词云

    // 词云数据：词和对应的权重
    if (!window.wordData) {
        window.wordData = [
            {text: "人工智能", weight: 10, color: "#FF5252"},
            {text: "机器学习", weight: 8, color: "#FF7043"},
            {text: "深度学习", weight: 9, color: "#FFCA28"},
            {text: "数据分析", weight: 7, color: "#66BB6A"},
            {text: "云计算", weight: 6, color: "#26C6DA"},
            {text: "区块链", weight: 5, color: "#42A5F5"},
            {text: "物联网", weight: 7, color: "#7E57C2"},
            {text: "大数据", weight: 8, color: "#EC407A"},
            {text: "虚拟现实", weight: 6, color: "#AB47BC"},
            {text: "增强现实", weight: 5, color: "#5C6BC0"}
        ];
    }

    // 获取DOM元素
    const wordCloud = document.getElementById('wordCloud');
    console.log(wordCloud);
    const popup = document.getElementById('popup');
    const popupContent = document.getElementById('popupContent');
    const closeBtn = document.getElementById('closeBtn');
    const overlay = document.getElementById('overlay');

    // 显示弹窗
    function showPopup(text) {
        popupContent.textContent = text;
        popup.style.display = 'block';
        overlay.style.display = 'block';
    }

    // 关闭弹窗
    closeBtn.addEventListener('click', () => {
        popup.style.display = 'none';
        overlay.style.display = 'none';
    });

    // 点击遮罩层也关闭弹窗
    overlay.addEventListener('click', () => {
        popup.style.display = 'none';
        overlay.style.display = 'none';
    });

    console.log(wordData);
    generateWordCloud();
    randomizeWordPositions();

}