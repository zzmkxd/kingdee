package plugins.course;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Button;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.list.BillList;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * 基础资料插件
 */
public class KnowpointsDataProcessor extends AbstractBasePlugIn implements Plugin {
        // 单据体标识
        private static final String ENTRY_ENTITY_KEY = "lag1_d3js_knowpoints";
        private static final String KNID = "lag1_knpid";
        private static final String KNNAME = "lag1_knowpname";
        private static final String KNPARENT = "lag1_knowpointparent";
        private static final String KNEXPAND = "lag1_expand";
        private static final String KNCHAP = "lag1_chap";
        private static final String KNDES = "lag1_description";
        private String rootName = "";

        // 需要提取的字段标识
        private final String[] TARGET_FIELDS = {
                "lag1_knpid",
                "lag1_knowpname",
                "lag1_knowpointparent",
                "lag1_expand",
                "lag1_chap",
                "lag1_description"
        };
        @Override
        public void registerListener(EventObject e) {
            super.registerListener(e);
            Button mind_btn = this.getView().getControl("lag1_viewmindmap");
            mind_btn.addClickListener(this);
        }

        @Override
        public void click(EventObject evt) {
            super.click(evt);
            Control source = (Control) evt.getSource();
            if(StringUtils.equals("lag1_viewmindmap",source.getKey())){
//                this.getView().showMessage("btn被点击");
                DynamicObject formData = this.getModel().getDataEntity();
                String Root_kPointName = null;
                showMessageForEachRowBatch();
            }
        }

    /**
     * 获取单据体信息
     */
    private void showMessageForEachRowBatch(){
        //获取单据体控件
        EntryGrid entryGrid = this.getView().getControl("lag1_knp");
        if(entryGrid == null){
            this.getView().showMessage("EntryGrid控件未找到！");
            return;
        }

        //获取单据体数据集合
        DynamicObjectCollection entryRows = this.getModel().getEntryEntity("lag1_knp");
        if(entryRows==null || entryRows.isEmpty()){
            this.getView().showMessage("entrygrid中没有数据！");
            return;
        }

        //用于收集所有信息
        StringBuilder messages = new StringBuilder();
        //1构建节点列表
        List<Map<String,Object>> nodes = new ArrayList<>();
        List<Map<String,Object>> links = new ArrayList<>();
        //手动添加根节点root
        rootName=this.getModel().getDataEntity().getString("name");
        nodes.add(createNode("0",rootName, 0,null,false,null));

        //遍历每一行数据
        for(DynamicObject entryRow : entryRows){
            String knid = (String) entryRow.get(KNID);
//            int knid = Integer.parseInt((String) entryRow.get(KNID));
            String knname = (String) entryRow.get(KNNAME);
            String knparent = (String) entryRow.get(KNPARENT);
//            if(Integer.parseInt(knparent)==0) knparent=null;    //根节点操作父节点为null
//            int knparent = Integer.parseInt((String) entryRow.get(KNPARENT));
            String knexpand = (String) entryRow.get(KNEXPAND);
//            int knexpand = Integer.parseInt((String) entryRow.get(KNEXPAND));
            String knchap = (String) entryRow.get(KNCHAP);
            String kndes = (String) entryRow.get(KNDES);

            nodes.add(createNode(knid,knname, Integer.parseInt(knexpand),knparent,false,kndes));
            links.add(createLink(knparent,knid,0.7));

            String message = knid+knname+knparent+knexpand+knchap+kndes+". ";
            messages.append(message);
        }
//        this.getView().showMessage(messages.toString());
        //构建最后JSON
        JSONObject courseJson = new JSONObject();
        courseJson.put("nodes",nodes);
        courseJson.put("links",links);
//        this.getView().showMessage(courseJson.toJSONString());
        System.out.println("json"+courseJson.toJSONString());
//        this.getView().returnDataToParent(courseJson.toJSONString());
        // 创建页面跳转参数
        FormShowParameter parameter = new FormShowParameter();
        parameter.getOpenStyle().setShowType(ShowType.Modal);
        parameter.setFormId("lag1_knowpointmap");

// 将JSON字符串放入参数中
        parameter.setCustomParam("treeData", courseJson.toJSONString());
// 打开目标页面
        this.getView().showForm(parameter);
    }

    @Override
        public void afterLoadData(EventObject e) {
            super.afterLoadData(e);
//            this.getView().showMessage(rootName);
        }

        private Map<String,Object> createNode(String id, String name, int group, String parentId, boolean expanded, String desc){
            Map<String, Object> node = new HashMap<>();
            node.put("id", id);
            node.put("name", name);
            node.put("group", group);
            node.put("expanded", expanded);
            node.put("description", desc);
            if(parentId!=null){
                node.put("parent",parentId);
            }
            return node;
        }

        // 创建连接辅助方法
        private Map<String, Object> createLink(String source, String target, double strength) {
            Map<String, Object> link = new HashMap<>();
            link.put("source", source);
            link.put("target", target);
            link.put("strength", strength);
            return link;
        }

}