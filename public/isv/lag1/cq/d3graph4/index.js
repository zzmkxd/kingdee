/**
 *  D3.js力导向图控件 - 基于KDApi的组件封装
 */
(function (KDApi, $) {
    // 构造函数
    function D3Graph(model) {
        this._setModel(model);
    }

    // 原型中封装生命周期函数
    D3Graph.prototype = {
        _setModel: function (model) {
            this.model = model;
        },

        init: function (props) {
            // 初始化函数
            initD3Graph(this.model, props || {});
        },

        update: function (props) {
            // 更新属性
            // 如果需要重新渲染，可以再次调用渲染函数
            renderGraph(this.model, props || {});
        },

        destoryed: function () {
            // 清理事件监听和DOM引用
            if (this.model && this.model.dom) {
                const resetButton = this.model.dom.querySelector('#resetZoom');
                const expandButton = this.model.dom.querySelector('#expandAll');
                const collapseButton = this.model.dom.querySelector('#collapseAll');
                const layoutSelect = this.model.dom.querySelector('#layoutType');

                if (resetButton) resetButton.onclick = null;
                if (expandButton) expandButton.onclick = null;
                if (collapseButton) collapseButton.onclick = null;
                if (layoutSelect) layoutSelect.onchange = null;
            }
            this.model = null;
        }
    };

    // 初始化函数
    var initD3Graph = function (model, props) {
        // console.log(props);
        // 1. 先加载CSS资源
        KDApi.loadFile('./css/graph.css', model, function () {
            // 2. 获取HTML模板并渲染
            KDApi.getTemplateStringByFilePath('./graph.html', model, {
                title: props.title || 'D3.js力导向图 - 交互式节点展开/折叠'
            }).then(function (result) {
                // 3. 设置HTML内容
                model.dom.innerHTML = result;

                // 4. 确保D3.js已加载

                    // 需要加载D3.js
                    KDApi.loadFile('./js/d3.v7.min.js', model, function () {
                        loadGraphScript(model, props);
                    });

            });
        });
    };

    // 加载图表脚本并渲染
    var loadGraphScript = function (model, props) {
        KDApi.loadFile('./js/graph.js', model, function () {
            // 在graph.js加载完成后渲染图表
            renderGraph(model, props);
        });
    };

    // 渲染图表并绑定事件
    var renderGraph = function (model, props) {
        // console.error(JSON.parse(props.data));
        // 确保DOM已经准备好
        if (!model.dom) return;

        // 绑定事件
        bindEvents(model, props);

        // 调用graph.js中的初始化函数，传入数据
        if (window.initGraph) {

            window.initGraph(model.dom, JSON.parse(props.data)|| {
                nodes: [
                    {id: "1", name: "根节点", group: 1, expanded: false, description: "这是根节点，点击可以展开子节点"},
                    {id: "2", name: "子节点1", group: 2, parent: "1", description: "一级子节点，属于根节点的直接子节点"},
                    {id: "3", name: "子节点2", group: 2, parent: "1", description: "一级子节点，属于根节点的直接子节点"},
                    {id: "4", name: "子节点3", group: 3, parent: "2", description: "二级子节点，属于子节点1的子节点"},
                    {id: "5", name: "子节点4", group: 3, parent: "2", description: "二级子节点，属于子节点1的子节点"},
                    {id: "6", name: "子节点5", group: 3, parent: "3", description: "二级子节点，属于子节点2的子节点"},
                    {id: "7", name: "子节点6", group: 3, parent: "3", description: "二级子节点，属于子节点2的子节点"},
                    {id: "8", name: "子节点7", group: 4, parent: "4", description: "三级子节点，属于子节点3的子节点"},
                    {id: "9", name: "子节点8", group: 4, parent: "6", description: "三级子节点，属于子节点5的子节点"},
                ],
                links: [
                    {source: "1", target: "2", strength: 0.7},
                    {source: "1", target: "3", strength: 0.7},
                    {source: "2", target: "4", strength: 0.5},
                    {source: "2", target: "5", strength: 0.5},
                    {source: "3", target: "6", strength: 0.5},
                    {source: "3", target: "7", strength: 0.5},
                    {source: "4", target: "8", strength: 0.3},
                    {source: "6", target: "9", strength: 0.3},
                ]
            });
        }
    };

    // 事件绑定函数
    var bindEvents = function (model, props) {
        if (!model.dom) return;

        const resetButton = model.dom.querySelector('#resetZoom');
        const expandButton = model.dom.querySelector('#expandAll');
        const collapseButton = model.dom.querySelector('#collapseAll');
        const layoutSelect = model.dom.querySelector('#layoutType');

        // 使用onclick而不是addEventListener，避免重复绑定
        if (resetButton) {
            resetButton.onclick = function (event) {
                if (typeof props.onResetClick === 'function') {
                    props.onResetClick(event);
                }
            };
        }

        if (expandButton) {
            expandButton.onclick = function (event) {
                if (typeof props.onExpandClick === 'function') {
                    props.onExpandClick(event);
                }
            };
        }

        if (collapseButton) {
            collapseButton.onclick = function (event) {
                if (typeof props.onCollapseClick === 'function') {
                    props.onCollapseClick(event);
                }
            };
        }

        // 添加布局选择事件绑定
        if (layoutSelect) {
            layoutSelect.onchange = function (event) {
                if (typeof props.onLayoutChange === 'function') {
                    props.onLayoutChange(event, this.value);
                }
            };
        }
    };

    // 注册控件
    KDApi.register('d3graph4', D3Graph);

})(window.KDApi); // 使用window.jQuery确保引用正确