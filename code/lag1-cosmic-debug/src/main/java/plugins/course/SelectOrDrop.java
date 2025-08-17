package plugins.course;

import com.kingdee.bos.qing.dfs.common.util.LogUtil;
import com.kingdee.cosmic.ctrl.kdf.tablepanel.CellStyle;
import kd.bos.base.AbstractBasePlugIn;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityType;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.list.BillList;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.MetadataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.*;

import static kd.bos.list.ListShowParameter.BILLLISTID;

/**
 * 基础资料插件
 */
public class SelectOrDrop extends AbstractBasePlugIn implements Plugin {
    private static final String SELECT_BTN = "lag1_selectcourse";
    private static final String DROP_BTN = "lag1_dropcourse";
    private static final String TK_COURSE = "lag1_course";
    private static final String TK_SELECTORDROP = "lag1_selectordrop";
    private static final String TD_NUMBER = "number";
    private static final String TD_COURSENUMBER = "lag1_number";
    private static final String TD_STUID = "name";
    private static final String TD_CHOSED = "lag1_chose";

    private static final String TK_USERDATA = "lag1_user_data";
    private static final String TK_KNPOINT = "lag1_knowpoints";
    private static final String TD_UDCODE = "number";
    private static final String TD_UDCOURSEID = "name";
    private static final String TD_UDUSERID = "creator";
    private static final String TD_UDKNPOINT = "lag1_linkkp";
    private static final String TD_UDANSNUM = "lag1_ans_num";
    private static final String TD_UDSUNSCORE = "lag1_sum_score";
    private static final String TD_UDdata = "lag1_data";
    private static final String TD_UDenable = "enbale";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        //注册工具栏点击事件
        Toolbar toolbar = this.getView().getControl("toolbarap");
        toolbar.addItemClickListener(this);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);
        String itemKey = evt.getItemKey();
        if(itemKey.equals(SELECT_BTN)){
            //选课
            onSelectClick();
        }else if(itemKey.equals(DROP_BTN)){
            //退课
            onDropClick();
        }
    }

    private void onDropClick() {
        IDataEntityType entityType = MetadataServiceHelper.getDataEntityType(TK_SELECTORDROP);
        List<String> selectedCourseIds = getSelectedCourseIds();
        if(selectedCourseIds == null || selectedCourseIds.isEmpty()) {
            this.getView().showMessage("请选中课程");
            return;
        }

        // 获取当前用户ID
        String currentUserId = String.valueOf(RequestContext.get().getCurrUserId());

        // 记录成功退课的课程数量
        int successCount = 0;

        for(String courseId : selectedCourseIds) {
            // 检查用户是否已选择该课程
            QFilter filter = new QFilter(TD_COURSENUMBER, QCP.equals, courseId);
            filter.and(new QFilter(TD_STUID, QCP.equals, currentUserId));
            DynamicObject existingRecord = BusinessDataServiceHelper.loadSingle(
                    TK_SELECTORDROP,
                    TD_NUMBER,
                    new QFilter[]{filter}
            );

            if(existingRecord != null) {
                try {
                    // 删除选课记录
                    BusinessDataServiceHelper.delete(
                            entityType,
                            new Object[]{existingRecord.getPkValue()}
                    );
                    // 更新课程表中的选课状态
                    DynamicObject course = BusinessDataServiceHelper.loadSingle(
                            TK_COURSE,
                            "number,lag1_chose",
                            new QFilter[]{new QFilter(TD_NUMBER, QCP.equals, courseId)}
                    );
                    if (course != null) {
                        course.set(TD_CHOSED, "未选"); // 设置为未选
                        SaveServiceHelper.saveOperate(TK_COURSE, new DynamicObject[]{course}, null);
                        this.getView().updateView(BILLLISTID);
                    }
//                    SaveServiceHelper.saveOperate(TK_COURSE, new DynamicObject[]{course}, null);
                    //删除相关的学情数据
                    deleteUserData(courseId,currentUserId);
                    successCount++;
                } catch (Exception e) {
                    LogUtil.error("退课失败，课程ID：" + courseId, e);
                    this.getView().showMessage("退课失败：" + e.getMessage());
                }
            } else {
                this.getView().showMessage("用户未选择课程：" + courseId);
            }
        }

        if(successCount > 0) {
            this.getView().showMessage("成功退课 " + successCount + " 门课程");
            // 刷新界面
            this.getView().updateView();
        }
    }

    /**
     * 删除学情数据函数
     * @param courseId
     * @param currentUserId
     */
    private void deleteUserData(String courseId, String currentUserId) {
        IDataEntityType entityType = MetadataServiceHelper.getDataEntityType(TK_USERDATA);
        QFilter userFilter = new QFilter(TD_UDCOURSEID,QCP.equals,courseId);
//        userFilter.and(new QFilter("creator.id",QCP.equals,Long.parseLong(currentUserId)));
        DynamicObject[] userDatas = BusinessDataServiceHelper.load(TK_USERDATA,"id",new QFilter[]{userFilter});
        for(DynamicObject userData: userDatas){
            BusinessDataServiceHelper.delete(entityType,new Object[]{userData.getPkValue()});
        }
    }

    private void onSelectClick() {
        List<String> selectedCourseIds = getSelectedCourseIds();
        if(selectedCourseIds==null || selectedCourseIds.isEmpty()){
            this.getView().showMessage("请选中课程");
            return;
        }

        //获取当前用户ids
        String currentUserId = String.valueOf(RequestContext.get().getCurrUserId());

        //构造新记录并保存
        for(String courseId : selectedCourseIds){
            //检查是否已经存在该用户对该课程的选课记录
            QFilter filter = new QFilter(TD_COURSENUMBER,QCP.equals,courseId);
            filter.and(new QFilter(TD_STUID,QCP.equals,currentUserId));
            DynamicObject existingRecord = BusinessDataServiceHelper.loadSingle(TK_SELECTORDROP,TD_NUMBER,new QFilter[]{filter});
            if(existingRecord==null){
                //创建新记录
                DynamicObject newRecord = BusinessDataServiceHelper.newDynamicObject(TK_SELECTORDROP);

                StringBuilder sb1 = new StringBuilder();
                for (int i = 1; i <= 10; i++) {
                    int ascii = 48 + (int) (Math.random() * 9);
                    char c = (char) ascii;
                    sb1.append(c);
                }
                //设置对应属性
                newRecord.set(TD_NUMBER, sb1.toString());
                newRecord.set(TD_COURSENUMBER,courseId);
                newRecord.set(TD_STUID,currentUserId);
                SaveServiceHelper.saveOperate(TK_SELECTORDROP,new DynamicObject[]{newRecord},null);

                // 更新课程表中的选课状态
                DynamicObject course = BusinessDataServiceHelper.loadSingle(
                        TK_COURSE,
                        "number,lag1_chose",
                        new QFilter[]{new QFilter(TD_NUMBER, QCP.equals, courseId)}
                );
//                this.getView().showMessage("course"+course);
                if (course != null) {
                    course.set(TD_CHOSED, "已选"); // 设置为已选
                    SaveServiceHelper.saveOperate(TK_COURSE, new DynamicObject[]{course}, null);
                    this.getView().updateView(BILLLISTID);
                    
                    //初始化学情数据
                    initUserData(courseId, currentUserId);
                }
            }else{
//                this.getView().showMessage("用户已经选过课程");
            }
        }
        this.getView().showMessage("选课成功");
    }

    private void initUserData(String courseId, String currentUserId) {
        DynamicObject[] knowpoints = BusinessDataServiceHelper.load(TK_KNPOINT, "id,name", new QFilter[]{new QFilter("lag1_courseid.number", QCP.equals, courseId)});
        // 打印知识点数组的内容
        StringBuilder knowpointsInfo = new StringBuilder("Knowpoints: [\n");
        for (DynamicObject kp : knowpoints) {
            knowpointsInfo.append("  ID: ").append(kp.getString("id")).append(", Name: ").append(kp.getString("name")).append("\n");
        }
        knowpointsInfo.append("]");
//        this.getView().showMessage(knowpointsInfo.toString());

        //初始化学情数据
        for(DynamicObject kp:knowpoints){
            DynamicObject userData = BusinessDataServiceHelper.newDynamicObject(TK_USERDATA);
            StringBuilder sb1 = new StringBuilder();
            for (int i = 1; i <= 10; i++) {
                int ascii = 48 + (int) (Math.random() * 9);
                char c = (char) ascii;
                sb1.append(c);
            }
            userData.set(TD_UDCODE,sb1);
            userData.set(TD_UDCOURSEID,courseId);
            userData.set(TD_UDUSERID,currentUserId);
            userData.set(TD_UDKNPOINT,kp);
            userData.set(TD_UDANSNUM,0);
            userData.set(TD_UDSUNSCORE,0);
            userData.set(TD_UDdata,0);
//            userData.set(TD_UDenable,"可用"); //使用状态
            SaveServiceHelper.saveOperate(TK_USERDATA,new DynamicObject[]{userData},null);
        }
    }

    private List<String> getSelectedCourseIds(){
        String fields = "id,number";
        List<String> courseIds = new ArrayList<>();
        BillList billList = this.getControl(BILLLISTID);
        if (billList == null) {
            this.getView().showMessage("BillList 控件未找到");
            return null;
        }

        ListSelectedRowCollection collection = billList.getSelectedRows();
        if(collection.isEmpty()) this.getView().showMessage("请选中课程");
        for(ListSelectedRow col : collection){
            Long selectId = (Long) col.getPrimaryKeyValue();
            QFilter qFilter = new QFilter("id", QCP.equals,selectId);
            DynamicObject dys = BusinessDataServiceHelper.loadSingle(TK_COURSE,"id,number",
                    new QFilter[]{qFilter});
            String courseId = dys.getString("number");
            courseIds.add(courseId);
        }
        return courseIds;
    }

    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);
        String currentUserId = String.valueOf(RequestContext.get().getCurrUserId());
        BillList billList = this.getControl(BILLLISTID);
        if (billList != null) {
            // 1. 加载所有课程数据
            DynamicObject[] allCourses = BusinessDataServiceHelper.load(TK_COURSE, "id,number,lag1_chose", null);

            // 2. 查询当前用户的所有选课记录
            QFilter userFilter = new QFilter(TD_STUID, QCP.equals, currentUserId);
            DynamicObject[] selectedCourses = BusinessDataServiceHelper.load(TK_SELECTORDROP, TD_COURSENUMBER, new QFilter[]{userFilter});

            // 3. 构建已选课程集合
            Set<String> selectedCourseNumbers = new HashSet<>();
            for (DynamicObject obj : selectedCourses) {
                selectedCourseNumbers.add(obj.getString(TD_COURSENUMBER));
            }

            // 4. 遍历所有课程并设置选课状态
            for (int i = 0; i < allCourses.length; i++) {
                String courseNumber = allCourses[i].getString("number");
                boolean isSelected = selectedCourseNumbers.contains(courseNumber);

                // 更新课程的 TD_CHOSED 字段
                allCourses[i].set("lag1_chose", isSelected ? "已选" : "未选");

//                // 方案1：绑定CSS类名（需前端预定义样式）
//                Map<String, Object> classProps = new HashMap<>();
//                classProps.put("className", isSelected ? "selected-course" : "unselected-course");
//                billList.setCustomProperties("lag1_chose", i, classProps);

//                // 方案2：直接内联样式（更可靠）
//                Map<String, Object> styleProps = new HashMap<>();
//                styleProps.put("style", "color: " + (isSelected ? "green" : "red"));
//                billList.setCustomProperties("lag1_chose", i, styleProps);

//                // 动态设置单元格的类名
//                String className = isSelected ? "selected-course" : "unselected-course";
//                // 直接设置样式而非类名
//                Map<String, Object> styleProps = new HashMap<>();
//                styleProps.put("style", "color: " + (isSelected ? "green" : "red"));
//                billList.setCustomProperties("lag1_chose", i, styleProps);

                // 设置列表控件中的选课状态
//                billList.setCellFieldValue(TD_CHOSED, i, isSelected ? "已选" : "未选");
            }

            // 5. 保存更新后的课程数据
            SaveServiceHelper.saveOperate(TK_COURSE, allCourses, null);

            // 6. 刷新视图
            billList.refresh();
            this.getView().updateView(BILLLISTID);
        } else {
            this.getView().showMessage("BillList 控件未找到");
        }
    }


}