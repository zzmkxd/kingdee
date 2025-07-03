// 图形组件封装
window.initGraph = function(domElement, data) {
    console.error(data);
    // 设置SVG容器
    const svg = d3.select("#d3js-svg");
    const width = svg.node().parentElement.clientWidth;
    const height = svg.node().parentElement.clientHeight;

    // 创建力模拟
    const simulation = d3
        .forceSimulation()
        .force(
            "link",
            d3.forceLink().id((d) => d.id).distance(100)
        )
        .force("charge", d3.forceManyBody().strength(-300).distanceMax(500))
        .force("center", d3.forceCenter(width / 2, height / 2))
        .force("collide", d3.forceCollide().radius(25).strength(0.7))
        .alphaDecay(0.05); // 减缓冷却速度，使布局更稳定

    // 定义数据集，包含子节点信息
    const fullData = data;

    // 创建箭头标记
    svg.append("defs").selectAll("marker")
        .data(["end"])      // 不同类型的标记
        .enter().append("marker")
        .attr("id", d => d)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", 25)   // 调整箭头位置，使其不覆盖节点
        .attr("refY", 0)
        .attr("markerWidth", 6)
        .attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("path")
        .attr("d", "M0,-5L10,0L0,5")
        .attr("fill", "#999");

    // 创建图形容器，支持缩放平移
    const container = svg.append("g");

    // 初始只显示根节点
    let visibleNodes = [fullData.nodes[0]];
    let visibleLinks = [];

    // 创建连接线容器
    let link = container
        .append("g")
        .attr("class", "links")
        .selectAll("line");

    // 创建节点容器
    let node = container
        .append("g")
        .attr("class", "nodes")
        .selectAll("circle");

    // 创建标签容器
    let labels = container
        .append("g")
        .attr("class", "labels")
        .selectAll("text");

    // 创建工具提示
    const tooltip = d3.select("#tooltip");

    // 节点信息面板
    const nodeInfo = d3.select("#nodeInfo");
    const nodeInfoTitle = d3.select("#nodeInfoTitle");
    const nodeInfoContent = d3.select("#nodeInfoContent");
    const nodeInfoClose = d3.select("#nodeInfoClose");

    // 关闭节点信息面板
    nodeInfoClose.on("click", () => {
        nodeInfo.style("display", "none");
    });

    // 更新图形的函数
    function updateGraph() {
        // 更新连接
        link = link.data(visibleLinks, (d) => `${d.source.id || d.source}-${d.target.id || d.target}`);
        link.exit().transition().duration(300)
            .attr("stroke-opacity", 0)
            .remove();
        const linkEnter = link.enter().append("line")
            .attr("stroke-width", d => d.strength ? d.strength * 3 : 1.5)
            .attr("stroke-opacity", 0)
            .attr("marker-end", "url(#end)"); // 添加箭头
        
        linkEnter.transition().duration(300)
            .attr("stroke-opacity", 0.6);
            
        link = linkEnter.merge(link);

        // 更新节点
        node = node.data(visibleNodes, (d) => d.id);
        node.exit().transition().duration(300)
            .attr("r", 0)
            .remove();
            
        const nodeEnter = node
            .enter()
            .append("circle")
            .attr("r", 0)
            .attr("fill", (d) => d3.schemeCategory10[d.group % 10])
            .call(
                d3
                    .drag()
                    .on("start", dragstarted)
                    .on("drag", dragged)
                    .on("end", dragended)
            );

        nodeEnter.transition().duration(300)
            .attr("r", d => d.group === 1 ? 15 : 10);

        // 添加点击事件
        nodeEnter.on("click", clickNode);

        // 添加双击事件 - 显示节点详情
        nodeEnter.on("dblclick", function(event, d) {
            event.stopPropagation();
            showNodeInfo(d);
        });

        // 添加悬停效果
        nodeEnter
            .on("mouseover", function (event, d) {
                // 显示工具提示
                tooltip
                    .style("display", "block")
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px")
                    .html(`<strong>${d.name}</strong><br>${d.description || ''}`);

                // 高亮当前节点
                d3.select(this).attr("stroke", "#000").attr("stroke-width", 2);

                // 高亮相关连接和节点
                const connectedNodes = visibleLinks
                    .filter((link) => 
                        (link.source.id === d.id || link.source === d.id) || 
                        (link.target.id === d.id || link.target === d.id)
                    )
                    .flatMap((link) => [
                        link.source.id || link.source, 
                        link.target.id || link.target
                    ]);

                link
                    .style("stroke", (l) =>
                        (l.source.id === d.id || l.source === d.id) || 
                        (l.target.id === d.id || l.target === d.id) 
                            ? "#000" : "#999"
                    )
                    .style("stroke-opacity", (l) =>
                        (l.source.id === d.id || l.source === d.id) || 
                        (l.target.id === d.id || l.target === d.id) 
                            ? 1 : 0.2
                    );

                node.style("opacity", (n) => 
                    connectedNodes.includes(n.id) ? 1 : 0.2
                );
                
                // 高亮相关标签
                labels.style("opacity", (n) => 
                    connectedNodes.includes(n.id) ? 1 : 0.2
                );
            })
            .on("mouseout", function () {
                // 隐藏工具提示
                tooltip.style("display", "none");
                
                // 恢复默认样式
                d3.select(this).attr("stroke", "#fff").attr("stroke-width", 1.5);
                link.style("stroke", "#999").style("stroke-opacity", 0.6);
                node.style("opacity", 1);
                labels.style("opacity", 1);
            });

        // 更新节点展开状态的视觉指示
        nodeEnter.classed("expanded", d => d.expanded);
        node.classed("expanded", d => d.expanded);

        node = nodeEnter.merge(node);

        // 更新标签
        labels = labels.data(visibleNodes, (d) => d.id);
        labels.exit().transition().duration(300)
            .attr("opacity", 0)
            .remove();
            
        const labelsEnter = labels
            .enter()
            .append("text")
            .text((d) => d.name)
            .attr("font-size", 10)
            .attr("dx", d => d.group === 1 ? 20 : 15)
            .attr("dy", 4)
            .attr("opacity", 0);
            
        labelsEnter.transition().duration(300)
            .attr("opacity", 1);
            
        labels = labelsEnter.merge(labels);

        // 重新启动模拟
        simulation.nodes(visibleNodes);
        simulation.force("link").links(visibleLinks);
        simulation.alpha(0.5).restart();
    }

    // 节点点击事件处理函数
    function clickNode(event, d) {
        // 防止事件冒泡
        event.stopPropagation();

        if (!d.expanded) {
            // 展开节点：找到所有以当前节点为父节点的子节点
            const childNodes = fullData.nodes.filter((node) => node.parent === d.id);
            
            // 如果没有子节点，不做任何操作
            if (childNodes.length === 0) {
                tooltip
                    .style("display", "block")
                    .style("left", (event.pageX + 10) + "px")
                    .style("top", (event.pageY - 10) + "px")
                    .html("该节点没有子节点");
                    
                setTimeout(() => {
                    tooltip.style("display", "none");
                }, 1500);
                return;
            }
            
            const childNodeIds = childNodes.map((node) => node.id);

            // 找到与这些子节点相关的连接
            const newLinks = fullData.links.filter(
                (link) =>
                    (link.source === d.id && childNodeIds.includes(link.target)) ||
                    (link.target === d.id && childNodeIds.includes(link.source))
            );

            // 为子节点设置初始位置，围绕父节点
            childNodes.forEach((node, i) => {
                // 计算围绕父节点的位置
                const angle = (i * (2 * Math.PI)) / childNodes.length;
                const radius = 100; // 增加距离父节点的半径
                node.x = d.x + radius * Math.cos(angle);
                node.y = d.y + radius * Math.sin(angle);
                // 添加一些随机性
                node.x += (Math.random() - 0.5) * 20;
                node.y += (Math.random() - 0.5) * 20;

                // 临时固定节点位置，稍后释放
                node.fx = node.x;
                node.fy = node.y;
            });

            // 添加到可见节点和连接
            visibleNodes = visibleNodes.concat(childNodes);
            visibleLinks = visibleLinks.concat(newLinks);

            // 标记为已展开
            d.expanded = true;

            // 更新图形
            updateGraph();

            // 800毫秒后释放节点位置，让力模拟接管
            setTimeout(() => {
                childNodes.forEach(node => {
                    node.fx = null;
                    node.fy = null;
                });
                simulation.alpha(0.3).restart(); // 轻微重启模拟
            }, 800);
        } else {
            // 折叠节点：移除所有子节点及其连接
            collapseNode(d);
            // 更新图形
            updateGraph();
        }
    }

    // 递归折叠节点的函数
    function collapseNode(d) {
        // 找到所有直接子节点
        const childNodes = visibleNodes.filter((node) => node.parent === d.id);

        // 对每个子节点递归折叠
        childNodes.forEach((child) => {
            if (child.expanded) {
                collapseNode(child);
            }
        });

        // 获取所有子节点ID
        const childNodeIds = childNodes.map((node) => node.id);

        // 移除相关连接
        visibleLinks = visibleLinks.filter(
            (link) => {
                const sourceId = link.source.id || link.source;
                const targetId = link.target.id || link.target;
                return !(
                    (sourceId === d.id && childNodeIds.includes(targetId)) ||
                    (targetId === d.id && childNodeIds.includes(sourceId))
                );
            }
        );

        // 移除子节点
        visibleNodes = visibleNodes.filter((node) => !childNodeIds.includes(node.id));

        // 标记为未展开
        d.expanded = false;
    }

    // 使用requestAnimationFrame优化更新
    function tick() {
        requestAnimationFrame(() => {
            link
                .attr("x1", (d) => d.source.x)
                .attr("y1", (d) => d.source.y)
                .attr("x2", (d) => d.target.x)
                .attr("y2", (d) => d.target.y);

            node
                .attr("cx", (d) => d.x)
                .attr("cy", (d) => d.y);
                
            labels
                .attr("x", (d) => d.x)
                .attr("y", (d) => d.y);
        });
    }

    // 设置tick回调
    simulation.on("tick", tick);

    // 拖拽相关函数
    function dragstarted(event, d) {
        if (!event.active) simulation.alphaTarget(0.3).restart();
        d.fx = d.x;
        d.fy = d.y;
    }

    function dragged(event, d) {
        d.fx = event.x;
        d.fy = event.y;
    }

    function dragended(event, d) {
        if (!event.active) simulation.alphaTarget(0);
        // 保持节点位置固定，除非用户双击释放
        // d.fx = null;
        // d.fy = null;
    }

    // 添加缩放和平移功能
    const zoom = d3.zoom()
        .scaleExtent([0.1, 5])
        .on("zoom", zoomed);

    svg.call(zoom);

    function zoomed(event) {
        container.attr("transform", event.transform);
    }

    // 重置缩放
    d3.select("#resetZoom").on("click", function() {
        svg.transition().duration(750).call(
            zoom.transform,
            d3.zoomIdentity
        );
    });

    // 展开所有节点
    d3.select("#expandAll").on("click", function() {
        // 先展开根节点
        const rootNode = visibleNodes.find(n => n.id === "1");
        if (rootNode && !rootNode.expanded) {
            clickNode({stopPropagation: () => {}}, rootNode);
        }
        
        // 然后展开所有一级节点
        setTimeout(() => {
            visibleNodes.filter(n => n.parent === "1").forEach(n => {
                if (!n.expanded) {
                    clickNode({stopPropagation: () => {}}, n);
                }
            });
        }, 1000);
    });

    // 折叠所有节点
    d3.select("#collapseAll").on("click", function() {
        const rootNode = visibleNodes.find(n => n.id === "1");
        if (rootNode && rootNode.expanded) {
            collapseNode(rootNode);
            updateGraph();
        }
    });

    // 搜索功能
    const searchInput = d3.select("#search");
    const searchResults = d3.select("#searchResults");
    
    searchInput.on("input", function() {
        const searchTerm = this.value.toLowerCase();
        if (searchTerm.length < 2) {
            searchResults.style("display", "none");
            return;
        }
        
        const matches = fullData.nodes.filter(node => 
            node.name.toLowerCase().includes(searchTerm) ||
            (node.description && node.description.toLowerCase().includes(searchTerm))
        );
        
        if (matches.length > 0) {
            searchResults.style("display", "block");
            searchResults.html("");
            
            matches.forEach(match => {
                searchResults.append("div")
                    .attr("class", "search-result-item")
                    .text(match.name)
                    .on("click", function() {
                        // 如果节点不可见，展开必要的父节点
                        if (!visibleNodes.some(n => n.id === match.id)) {
                            expandPathToNode(match.id);
                        }
                        
                        // 高亮并居中显示找到的节点
                        highlightNode(match.id);
                        
                        // 清空搜索框和结果
                        searchInput.property("value", "");
                        searchResults.style("display", "none");
                    });
            });
        } else {
            searchResults.style("display", "block");
            searchResults.html("<div class='search-result-item'>无匹配结果</div>");
        }
    });
    
    // 点击其他地方时隐藏搜索结果
    d3.select("body").on("click", function() {
        searchResults.style("display", "none");
    });
    
    // 阻止搜索框的点击事件冒泡
    searchInput.on("click", function() {
        d3.event.stopPropagation();
    });

    // 展开到指定节点的路径
    function expandPathToNode(nodeId) {
        const node = fullData.nodes.find(n => n.id === nodeId);
        if (!node) return;
        
        // 找到从根节点到目标节点的路径
        const path = [];
        let currentNode = node;
        
        while (currentNode.parent) {
            path.unshift(currentNode);
            currentNode = fullData.nodes.find(n => n.id === currentNode.parent);
        }
        
        // 添加根节点
        path.unshift(fullData.nodes[0]);
        
        // 依次展开路径上的节点
        let delay = 0;
        for (let i = 0; i < path.length - 1; i++) {
            const parentNode = path[i];
            
            // 如果父节点不在可见节点中，等待下一次更新
            if (!visibleNodes.some(n => n.id === parentNode.id)) {
                continue;
            }
            
            // 如果父节点未展开，展开它
            const visibleParent = visibleNodes.find(n => n.id === parentNode.id);
            if (visibleParent && !visibleParent.expanded) {
                setTimeout(() => {
                    clickNode({stopPropagation: () => {}}, visibleParent);
                }, delay);
                delay += 500;
            }
        }
    }

    // 高亮并居中显示节点
    function highlightNode(nodeId) {
        const targetNode = visibleNodes.find(n => n.id === nodeId);
        if (!targetNode) return;
        
        // 高亮节点
        node.filter(n => n.id === nodeId)
            .transition()
            .duration(500)
            .attr("r", d => d.group === 1 ? 20 : 15)
            .attr("stroke", "#ff0000")
            .attr("stroke-width", 3)
            .transition()
            .duration(500)
            .attr("r", d => d.group === 1 ? 15 : 10)
            .attr("stroke", d => d.expanded ? "#ff9900" : "#fff")
            .attr("stroke-width", d => d.expanded ? 2.5 : 1.5);
        
        // 居中显示节点
        const transform = d3.zoomTransform(svg.node());
        const scale = transform.k;
        const x = -targetNode.x * scale + width / 2;
        const y = -targetNode.y * scale + height / 2;
        
        svg.transition().duration(750).call(
            zoom.transform,
            d3.zoomIdentity.translate(x, y).scale(scale)
        );
    }

    // 显示节点详情
    function showNodeInfo(d) {
        nodeInfoTitle.text(d.name);
        
        // 构建详情内容
        let content = `<p><strong>ID:</strong> ${d.id}</p>`;
        content += `<p><strong>类型:</strong> ${getNodeTypeName(d.group)}</p>`;
        if (d.parent) {
            const parentNode = fullData.nodes.find(n => n.id === d.parent);
            content += `<p><strong>父节点:</strong> ${parentNode ? parentNode.name : d.parent}</p>`;
        }
        
        // 查找子节点
        const childNodes = fullData.nodes.filter(n => n.parent === d.id);
        if (childNodes.length > 0) {
            content += `<p><strong>子节点:</strong> ${childNodes.length}个</p>`;
            content += `<ul>`;
            childNodes.forEach(child => {
                content += `<li>${child.name}</li>`;
            });
            content += `</ul>`;
        }
        
        if (d.description) {
            content += `<p><strong>描述:</strong> ${d.description}</p>`;
        }
        
        nodeInfoContent.html(content);
        nodeInfo.style("display", "block");
    }

    // 获取节点类型名称
    function getNodeTypeName(group) {
        switch(group) {
            case 1: return "根节点";
            case 2: return "一级节点";
            case 3: return "二级节点";
            case 4: return "三级节点";
            default: return `类型${group}`;
        }
    }

    // 布局类型切换
    d3.select("#layoutType").on("change", function() {
        const layoutType = this.value;
        
        // 停止当前模拟
        simulation.stop();
        
        switch(layoutType) {
            case "force":
                // 恢复力导向布局
                simulation
                    .force("link", d3.forceLink().id((d) => d.id).distance(100))
                    .force("charge", d3.forceManyBody().strength(-300).distanceMax(500))
                    .force("center", d3.forceCenter(width / 2, height / 2))
                    .force("collide", d3.forceCollide().radius(25).strength(0.7));
                break;
                
            case "radial":
                // 放射状布局
                simulation
                    .force("link", d3.forceLink().id((d) => d.id).distance(100))
                    .force("charge", d3.forceManyBody().strength(-100))
                    .force("r", d3.forceRadial(function(d) {
                        return d.group * 100; // 根据组确定半径
                    }, width / 2, height / 2).strength(1))
                    .force("center", null) // 移除中心力
                    .force("collide", d3.forceCollide().radius(25).strength(0.7));
                break;
                
            case "tree":
                // 树形布局
                // 释放所有节点的固定位置
                visibleNodes.forEach(node => {
                    node.fx = null;
                    node.fy = null;
                });
                
                // 找到根节点
                const root = visibleNodes.find(n => n.id === "1");
                if (root) {
                    // 固定根节点在中心
                    root.fx = width / 2;
                    root.fy = height / 3;
                    
                    // 设置每层节点的y坐标
                    visibleNodes.forEach(node => {
                        if (node.group > 1) {
                            node.fy = height / 3 + (node.group - 1) * 150;
                        }
                    });
                }
                
                simulation
                    .force("link", d3.forceLink().id((d) => d.id).distance(150).strength(1))
                    .force("charge", d3.forceManyBody().strength(-1000))
                    .force("center", null) // 移除中心力
                    .force("x", d3.forceX(width / 2).strength(0.1)) // 水平方向轻微的中心力
                    .force("y", d3.forceY().strength(0.3).y(d => d.fy || height / 2)) // 垂直方向强制按层级排列
                    .force("collide", d3.forceCollide().radius(50).strength(0.7));
                break;
        }
        
        // 重启模拟
        simulation.alpha(1).restart();
    });

    // 初始化图形
    updateGraph();

    // 窗口大小调整时重新计算尺寸
    window.addEventListener("resize", function() {
        const newWidth = svg.node().parentElement.clientWidth;
        const newHeight = svg.node().parentElement.clientHeight;
        
        // 更新力模拟中心
        simulation.force("center", d3.forceCenter(newWidth / 2, newHeight / 2));
        simulation.alpha(0.3).restart();
    });
}