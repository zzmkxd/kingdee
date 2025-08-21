window.initGraph = function (domElement, data) {
    const svg = d3.select("#d3js-svg"), width = svg.node().parentElement.clientWidth,
        height = svg.node().parentElement.clientHeight, g = svg.append("g");

    const sim = d3.forceSimulation()
        .force("link", d3.forceLink().id(d => d.id).distance(100))
        .force("charge", d3.forceManyBody().strength(-300).distanceMax(500))
        .force("center", d3.forceCenter(width / 2, height / 2))
        .force("collide", d3.forceCollide().radius(25).strength(0.7));

    svg.append("defs").append("marker")
        .attr("id", "end").attr("viewBox", "0 -5 10 10")
        .attr("refX", 25).attr("refY", 0).attr("markerWidth", 6).attr("markerHeight", 6)
        .attr("orient", "auto")
        .append("path").attr("d", "M0,-5L10,0L0,5").attr("fill", "#999");

    let visibleNodes = [data.nodes[0]], visibleLinks = [];
    let link = g.append("g").attr("class", "links").selectAll("line"),
        node = g.append("g").attr("class", "nodes").selectAll("circle"),
        label = g.append("g").attr("class", "labels").selectAll("text");

    const tooltip = d3.select("#tooltip"), info = d3.select("#nodeInfo");

    function update() {
        link = link.data(visibleLinks, d => `${d.source.id || d.source}-${d.target.id || d.target}`)
            .join(enter => enter.append("line")
                .attr("stroke", "#999").attr("stroke-width", d => d.strength ? d.strength * 3 : 1.5)
                .attr("marker-end", "url(#end)").style("opacity", 0)
                .call(e => e.transition().duration(300).style("opacity", .6)));

        node = node.data(visibleNodes, d => d.id).join(enter => enter.append("circle")
            .attr("r", 0).attr("fill", d => d3.schemeCategory10[d.group % 10])
            .call(d3.drag().on("start", drag).on("drag", drag).on("end", dragEnd))
            .on("click", (e, d) => toggleNode(d, e))
            .on("dblclick", (e, d) => showInfo(d))
            .on("mouseover", (e, d) => hoverNode(d, true, e))
            .on("mouseout", (e, d) => hoverNode(d, false, e))
            .call(e => e.transition().duration(300).attr("r", d => d.group === 1 ? 15 : 10)), update => update);

        label = label.data(visibleNodes, d => d.id).join(enter => enter.append("text").text(d => d.name).attr("font-size", 10)
            .attr("dx", d => d.group === 1 ? 20 : 15).attr("dy", 4).style("opacity", 0)
            .call(e => e.transition().duration(300).style("opacity", 1)));

        sim.nodes(visibleNodes).force("link").links(visibleLinks);
        sim.alpha(0.5).restart();
    }

    function toggleNode(d, event) {
        event?.stopPropagation();
        if (!d.expanded) {
            const children = data.nodes.filter(n => n.parent === d.id);
            if (!children.length) return showTip("该节点没有子节点", event);
            const ids = children.map(c => c.id);
            visibleNodes.push(...children);
            visibleLinks.push(...data.links.filter(l => (l.source === d.id && ids.includes(l.target)) || (l.target === d.id && ids.includes(l.source))));
            d.expanded = true;
            update();
        } else {
            collapse(d);
            update();
        }
    }

    function collapse(d) {
        const kids = visibleNodes.filter(n => n.parent === d.id);
        kids.forEach(c => c.expanded && collapse(c));
        const ids = kids.map(c => c.id);
        visibleLinks = visibleLinks.filter(l => !ids.includes(l.source.id || l.source) && !ids.includes(l.target.id || l.target));
        visibleNodes = visibleNodes.filter(n => !ids.includes(n.id));
        d.expanded = false;
    }

    function hoverNode(d, on, e) {
        if (on) {
            tooltip.style("display", "block").style("left", e.pageX + 10 + "px").style("top", e.pageY - 10 + "px")
                .html(`<b>${d.name}</b><br>${d.description || ''}`);
            node.attr("opacity", n => isConnected(d, n) ? 1 : .2);
            link.attr("stroke", l => isConnected(d, l.source) || isConnected(d, l.target) ? "#000" : "#999")
                .style("opacity", l => isConnected(d, l.source) || isConnected(d, l.target) ? 1 : .2);
            label.style("opacity", n => isConnected(d, n) ? 1 : .2);
        } else {
            tooltip.style("display", "none");
            node.attr("opacity", 1);
            link.attr("stroke", "#999").style("opacity", .6);
            label.style("opacity", 1);
        }
    }

    const isConnected = (a, b) => a.id === b.id || visibleLinks.some(l => (l.source.id || l.source) === a.id && (l.target.id || l.target) === b.id);

    function showTip(msg, e) {
        tooltip.style("display", "block").style("left", e.pageX + 10 + "px").style("top", e.pageY - 10 + "px").html(msg);
        setTimeout(() => tooltip.style("display", "none"), 1500);
    }

    function showInfo(d) {
        info.style("display", "block").html(`<h3>${d.name}</h3><p>ID:${d.id}</p>`);
    }

    function drag(e, d) {
        if (!e.active) sim.alphaTarget(0.3).restart();
        d.fx = e.x;
        d.fy = e.y;
    }

    function dragEnd(e, d) {
        if (!e.active) sim.alphaTarget(0);
    }

    sim.on("tick", () => {
        link.attr("x1", d => d.source.x).attr("y1", d => d.source.y).attr("x2", d => d.target.x).attr("y2", d => d.target.y);
        node.attr("cx", d => d.x).attr("cy", d => d.y);
        label.attr("x", d => d.x).attr("y", d => d.y);
    });

    svg.call(d3.zoom().scaleExtent([.1, 5]).on("zoom", e => g.attr("transform", e.transform)));
    update();


    // 搜索功能
    const searchInput = d3.select("#search");
    const searchResults = d3.select("#searchResults");

    searchInput.on("input", function () {
        const term = this.value.toLowerCase();
        if (term.length < 2) return searchResults.style("display", "none");

        const matches = data.nodes.filter(n => n.name.toLowerCase().includes(term) || (n.description && n.description.toLowerCase().includes(term)));

        searchResults.html("").style("display", "block");
        if (!matches.length) return searchResults.html("<div>无匹配结果</div>");

        matches.forEach(m => {
            searchResults.append("div").attr("class", "search-result-item")
                .text(m.name)
                .on("click", () => {
                    if (!visibleNodes.some(n => n.id === m.id)) expandPathToNode(m.id);
                    highlightNode(m.id);
                    searchInput.property("value", "");
                    searchResults.style("display", "none");
                });
        });
    });

// 展开到指定节点路径
    function expandPathToNode(id) {
        let cur = data.nodes.find(n => n.id === id);
        while (cur?.parent) {
            const p = visibleNodes.find(n => n.id === cur.parent);
            if (p && !p.expanded) toggleNode(p);
            cur = data.nodes.find(n => n.id === cur.parent);
        }
    }

// 高亮并居中
    function highlightNode(id) {
        const target = visibleNodes.find(n => n.id === id);
        if (!target) return;
        node.filter(n => n.id === id)
            .transition().duration(300)
            .attr("r", 20).attr("stroke", "#f00").attr("stroke-width", 3)
            .transition().duration(500)
            .attr("r", d => d.group === 1 ? 15 : 10).attr("stroke", "#fff").attr("stroke-width", 1.5);

        const t = d3.zoomTransform(svg.node()), scale = t.k;
        svg.transition().duration(750).call(d3.zoom().transform, d3.zoomIdentity.translate(width / 2 - target.x * scale, height / 2 - target.y * scale).scale(scale));
    }

// 布局切换
    d3.select("#layoutType").on("change", function () {
        const type = this.value;
        sim.stop();

        if (type === "force") {
            sim.force("link", d3.forceLink().id(d => d.id).distance(100))
                .force("charge", d3.forceManyBody().strength(-300).distanceMax(500))
                .force("center", d3.forceCenter(width / 2, height / 2));
        }
        if (type === "radial") {
            sim.force("r", d3.forceRadial(d => d.group * 100, width / 2, height / 2).strength(1))
                .force("center", null);
        }
        if (type === "tree") {
            const root = visibleNodes.find(n => n.id === "1");
            if (root) {
                root.fx = width / 2;
                root.fy = height / 3;
            }
            visibleNodes.forEach(n => {
                if (n.group > 1) n.fy = height / 3 + (n.group - 1) * 150;
            });
            sim.force("y", d3.forceY(d => d.fy || height / 2).strength(.3))
                .force("x", d3.forceX(width / 2).strength(.1))
                .force("center", null);
        }

        sim.alpha(1).restart();
    });
// 批量展开/折叠
    d3.select("#expandAll").on("click", () => {
        visibleNodes.forEach(n => {
            if (hasChildren(n) && !n.expanded) toggleNode(n);
        });
    });

    d3.select("#collapseAll").on("click", () => {
        // 根节点保留，其余都折叠
        visibleNodes.slice().forEach(n => {
            if (n.id !== "1" && n.expanded) toggleNode(n);
        });
    });

// 判断节点是否有子节点
    function hasChildren(node) {
        return data.links.some(l => l.source.id === node.id || l.source === node.id);
    }

};
