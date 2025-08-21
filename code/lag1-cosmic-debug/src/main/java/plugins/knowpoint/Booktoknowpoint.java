package plugins.knowpoint;

import kd.bos.base.AbstractBasePlugIn;
import kd.bos.base.BaseShowParameter;
import kd.bos.bill.BillShowParameter;
import kd.bos.bill.OperationStatus;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.StyleCss;
import kd.bos.form.control.Button;
import kd.bos.form.control.Toolbar;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.control.events.ItemClickListener;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.list.IListView;
import kd.bos.mvc.base.BaseView;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.sdk.plugin.Plugin;
import org.apache.commons.lang3.StringUtils;

import java.util.EventObject;

/**
 * 动态表单插件
 */
public class Booktoknowpoint extends AbstractBasePlugIn implements ItemClickListener {
    private final String TKBOOK="lag1_book";
        //回调方法的标识符
//    private static final String CALLBACK_ID = "parent_refresh1";

        //注册点击事件
        @Override
        public void registerListener(EventObject e) {
            // 注册点击事件
            super.registerListener(e);
            this.addItemClickListeners("tbmain");
        }
    DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache("customRegion");
        @Override
        public void itemClick(ItemClickEvent evt) {
            super.itemClick(evt);
            String itemKey = evt.getItemKey();
            if("lag1_button_toknowpoint".equals(itemKey)){
                String str = cache.get("damn");
                if( str == ""){
                    this.getView().showMessage("还未提取并转换教材文本为JSON数据！");
                    return;
                }
                if(IfCourseknpBuilded()){
                    this.getView().showMessage("课程已有知识点方案，请勿重复创建！");
                    return;
                }

                IDataModel formDataModel = this.getModel();
                DynamicObject formDataEntity = formDataModel.getDataEntity();
                Long formPkId = (Long) formDataEntity.getPkValue();
                DynamicObject postSingle = BusinessDataServiceHelper.loadSingle(TKBOOK, new QFilter[]{new QFilter("id", QCP.equals,formPkId)});
                if(postSingle!=null){
                    String targetForm = "lag1_d3js_knowpoints";//知识点-基础资料表单
                    String bookNumber = postSingle.getString("number");//教材编码
                    if(StringUtils.isNotBlank(bookNumber)){
                        //创建参数对象
//                    这里如果要打开的页面类型不同构造的是不一样的 typora里面有片可以抄
                        BaseShowParameter baseShowParameter = new BaseShowParameter();
                        baseShowParameter.setFormId(targetForm);
                        baseShowParameter.getOpenStyle().setShowType(ShowType.Modal);
                        // 关键点：必须设置表单状态
                        baseShowParameter.setStatus(OperationStatus.ADDNEW); // 或 OperationStatus.VIEW/EDIT/ADDNEW
                        this.getView().showForm(baseShowParameter);
                        //传递帖子编码作为自定义参数
                        baseShowParameter.setCustomParam("bookNumber", bookNumber); //这里是发送端在设置要传的参数
                        baseShowParameter.setCustomParam("pkId",formPkId);
                        //设置宽高
                        StyleCss styleCss = new StyleCss();
                        styleCss.setHeight("600");      //如果设置了宽高就有点像弹窗
                        styleCss.setWidth("1200");
                        baseShowParameter.getOpenStyle().setInlineStyleCss(styleCss);       //这几行是css样式
                        SaveServiceHelper.update(postSingle); // 这一步至关重要！ 更新数据
                        //打开下一个表单并传递参数
                        this.getView().showForm(baseShowParameter);     //这里是最后一步
                    }else{
                        this.getView().showMessage("错误");
                    }

                }else{
                    this.getView().showMessage("错误");
                }
            }

        }
//    private String jsonKnp = "{\"knowledgePoints\":[{\"description\":\"\",\"knowpExpand\":\"1\",\"chap\":\"1\",\"knowpointParent\":\"0\",\"knpId\":\"1\",\"knowpName\":\"马克思主义基本原理概述\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"1.1\",\"knowpointParent\":\"1\",\"knpId\":\"1.1\",\"knowpName\":\"马克思主义的科学内涵\"},{\"description\":\"关于自然、社会和人类思维发展一般规律的学说\",\"knowpExpand\":\"3\",\"chap\":\"1.1.1\",\"knowpointParent\":\"1.1\",\"knpId\":\"1.1.1\",\"knowpName\":\"马克思主义的定义\"},{\"description\":\"马克思主义哲学、政治经济学、科学社会主义\",\"knowpExpand\":\"3\",\"chap\":\"1.1.2\",\"knowpointParent\":\"1.1\",\"knpId\":\"1.1.2\",\"knowpName\":\"马克思主义的三个基本组成部分\"},{\"description\":\"体现科学性和革命性的统一\",\"knowpExpand\":\"3\",\"chap\":\"1.1.3\",\"knowpointParent\":\"1.1\",\"knpId\":\"1.1.3\",\"knowpName\":\"马克思主义的根本性质\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"1.2\",\"knowpointParent\":\"1\",\"knpId\":\"1.2\",\"knowpName\":\"马克思主义的历史发展\"},{\"description\":\"19世纪40年代资本主义发展和工人运动兴起\",\"knowpExpand\":\"3\",\"chap\":\"1.2.1\",\"knowpointParent\":\"1.2\",\"knpId\":\"1.2.1\",\"knowpName\":\"马克思主义的创立背景\"},{\"description\":\"马克思、恩格斯的理论贡献\",\"knowpExpand\":\"3\",\"chap\":\"1.2.2\",\"knowpointParent\":\"1.2\",\"knpId\":\"1.2.2\",\"knowpName\":\"马克思主义的主要创始人\"},{\"description\":\"从经典到现代的发展演变\",\"knowpExpand\":\"3\",\"chap\":\"1.2.3\",\"knowpointParent\":\"1.2\",\"knpId\":\"1.2.3\",\"knowpName\":\"马克思主义的发展历程\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"1.3\",\"knowpointParent\":\"1\",\"knpId\":\"1.3\",\"knowpName\":\"马克思主义的基本特征\"},{\"description\":\"揭示人类社会发展规律的科学理论\",\"knowpExpand\":\"3\",\"chap\":\"1.3.1\",\"knowpointParent\":\"1.3\",\"knpId\":\"1.3.1\",\"knowpName\":\"科学性特征\"},{\"description\":\"以人民为中心的理论体系\",\"knowpExpand\":\"3\",\"chap\":\"1.3.2\",\"knowpointParent\":\"1.3\",\"knpId\":\"1.3.2\",\"knowpName\":\"人民性特征\"},{\"description\":\"指导改造世界的行动指南\",\"knowpExpand\":\"3\",\"chap\":\"1.3.3\",\"knowpointParent\":\"1.3\",\"knpId\":\"1.3.3\",\"knowpName\":\"实践性特征\"},{\"description\":\"\",\"knowpExpand\":\"1\",\"chap\":\"2\",\"knowpointParent\":\"0\",\"knpId\":\"2\",\"knowpName\":\"世界的物质性及发展规律\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"2.1\",\"knowpointParent\":\"2\",\"knpId\":\"2.1\",\"knowpName\":\"世界的物质统一性\"},{\"description\":\"标志客观实在的哲学范畴\",\"knowpExpand\":\"3\",\"chap\":\"2.1.1\",\"knowpointParent\":\"2.1\",\"knpId\":\"2.1.1\",\"knowpName\":\"物质的哲学定义\"},{\"description\":\"运动是物质的存在方式\",\"knowpExpand\":\"3\",\"chap\":\"2.1.2\",\"knowpointParent\":\"2.1\",\"knpId\":\"2.1.2\",\"knowpName\":\"物质的根本属性\"},{\"description\":\"时间与空间是物质的基本存在形式\",\"knowpExpand\":\"3\",\"chap\":\"2.1.3\",\"knowpointParent\":\"2.1\",\"knpId\":\"2.1.3\",\"knowpName\":\"物质的存在形式\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"2.2\",\"knowpointParent\":\"2\",\"knpId\":\"2.2\",\"knowpName\":\"物质世界的二重分化\"},{\"description\":\"人类劳动创造了社会存在\",\"knowpExpand\":\"3\",\"chap\":\"2.2.1\",\"knowpointParent\":\"2.2\",\"knpId\":\"2.2.1\",\"knowpName\":\"自然界与人类社会的分化\"},{\"description\":\"人的意识活动形成主观世界\",\"knowpExpand\":\"3\",\"chap\":\"2.2.2\",\"knowpointParent\":\"2.2\",\"knpId\":\"2.2.2\",\"knowpName\":\"客观世界与主观世界的分化\"},{\"description\":\"使世界更加丰富多样\",\"knowpExpand\":\"3\",\"chap\":\"2.2.3\",\"knowpointParent\":\"2.2\",\"knpId\":\"2.2.3\",\"knowpName\":\"二重分化的意义\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"2.3\",\"knowpointParent\":\"2\",\"knpId\":\"2.3\",\"knowpName\":\"物质与意识的辩证关系\"},{\"description\":\"物质是第一性存在的观点\",\"knowpExpand\":\"3\",\"chap\":\"2.3.1\",\"knowpointParent\":\"2.3\",\"knpId\":\"2.3.1\",\"knowpName\":\"物质决定意识\"},{\"description\":\"意识具有反作用的特征\",\"knowpExpand\":\"3\",\"chap\":\"2.3.2\",\"knowpointParent\":\"2.3\",\"knpId\":\"2.3.2\",\"knowpName\":\"意识对物质的能动作用\"},{\"description\":\"物质与意识相互依存相互影响\",\"knowpExpand\":\"3\",\"chap\":\"2.3.3\",\"knowpointParent\":\"2.3\",\"knpId\":\"2.3.3\",\"knowpName\":\"辩证统一关系\"},{\"description\":\"\",\"knowpExpand\":\"1\",\"chap\":\"3\",\"knowpointParent\":\"0\",\"knpId\":\"3\",\"knowpName\":\"实践与认识及其发展规律\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"3.1\",\"knowpointParent\":\"3\",\"knpId\":\"3.1\",\"knowpName\":\"实践与认识的基本问题\"},{\"description\":\"实践是人类能动地改造世界的客观物质活动\",\"knowpExpand\":\"3\",\"chap\":\"3.1.1\",\"knowpointParent\":\"3.1\",\"knpId\":\"3.1.1\",\"knowpName\":\"实践的本质与结构\"},{\"description\":\"认识是主体对客体的能动反映\",\"knowpExpand\":\"3\",\"chap\":\"3.1.2\",\"knowpointParent\":\"3.1\",\"knpId\":\"3.1.2\",\"knowpName\":\"认识的本质与过程\"},{\"description\":\"实践决定认识，认识反作用于实践\",\"knowpExpand\":\"3\",\"chap\":\"3.1.3\",\"knowpointParent\":\"3.1\",\"knpId\":\"3.1.3\",\"knowpName\":\"实践与认识的辩证运动\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"3.2\",\"knowpointParent\":\"3\",\"knpId\":\"3.2\",\"knowpName\":\"真理与价值\"},{\"description\":\"真理具有客观标准但受历史条件限制\",\"knowpExpand\":\"3\",\"chap\":\"3.2.1\",\"knowpointParent\":\"3.2\",\"knpId\":\"3.2.1\",\"knowpName\":\"真理的客观性与相对性\"},{\"description\":\"实践是检验真理的唯一标准\",\"knowpExpand\":\"3\",\"chap\":\"3.2.2\",\"knowpointParent\":\"3.2\",\"knpId\":\"3.2.2\",\"knowpName\":\"真理的检验标准\"},{\"description\":\"真理追求和价值实现的统一\",\"knowpExpand\":\"3\",\"chap\":\"3.2.3\",\"knowpointParent\":\"3.2\",\"knpId\":\"3.2.3\",\"knowpName\":\"真理与价值的统一\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"3.3\",\"knowpointParent\":\"3\",\"knpId\":\"3.3\",\"knowpName\":\"认识世界与改造世界\"},{\"description\":\"改造世界是认识的根本目的\",\"knowpExpand\":\"3\",\"chap\":\"3.3.1\",\"knowpointParent\":\"3.3\",\"knpId\":\"3.3.1\",\"knowpName\":\"认识的目的\"},{\"description\":\"一切从实际出发、主观符合客观\",\"knowpExpand\":\"3\",\"chap\":\"3.3.2\",\"knowpointParent\":\"3.3\",\"knpId\":\"3.3.2\",\"knowpName\":\"实事求是的原则\"},{\"description\":\"形成良性互动关系\",\"knowpExpand\":\"3\",\"chap\":\"3.3.3\",\"knowpointParent\":\"3.3\",\"knpId\":\"3.3.3\",\"knowpName\":\"理论创新与实践创新\"},{\"description\":\"\",\"knowpExpand\":\"1\",\"chap\":\"4\",\"knowpointParent\":\"0\",\"knpId\":\"4\",\"knowpName\":\"人类社会及其发展规律\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"4.1\",\"knowpointParent\":\"4\",\"knpId\":\"4.1\",\"knowpName\":\"社会存在与社会意识\"},{\"description\":\"社会存在是社会意识的决定因素\",\"knowpExpand\":\"3\",\"chap\":\"4.1.1\",\"knowpointParent\":\"4.1\",\"knpId\":\"4.1.1\",\"knowpName\":\"社会存在决定社会意识\"},{\"description\":\"社会意识对社会存在具有反作用\",\"knowpExpand\":\"3\",\"chap\":\"4.1.2\",\"knowpointParent\":\"4.1\",\"knpId\":\"4.1.2\",\"knowpName\":\"社会意识的相对独立性\"},{\"description\":\"生产力与生产关系、经济基础与上层建筑的矛盾\",\"knowpExpand\":\"3\",\"chap\":\"4.1.3\",\"knowpointParent\":\"4.1\",\"knpId\":\"4.1.3\",\"knowpName\":\"社会基本矛盾\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"4.2\",\"knowpointParent\":\"4\",\"knpId\":\"4.2\",\"knowpName\":\"社会历史发展的动力\"},{\"description\":\"推动社会发展的根本动力\",\"knowpExpand\":\"3\",\"chap\":\"4.2.1\",\"knowpointParent\":\"4.2\",\"knpId\":\"4.2.1\",\"knowpName\":\"社会基本矛盾的作用\"},{\"description\":\"阶级社会发展的直接动力\",\"knowpExpand\":\"3\",\"chap\":\"4.2.2\",\"knowpointParent\":\"4.2\",\"knpId\":\"4.2.2\",\"knowpName\":\"阶级斗争的作用\"},{\"description\":\"科技进步推动社会发展的重要力量\",\"knowpExpand\":\"3\",\"chap\":\"4.2.3\",\"knowpointParent\":\"4.2\",\"knpId\":\"4.2.3\",\"knowpName\":\"科学技术的作用\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"4.3\",\"knowpointParent\":\"4\",\"knpId\":\"4.3\",\"knowpName\":\"人民群众在历史中的作用\"},{\"description\":\"人民群众是社会历史的主体\",\"knowpExpand\":\"3\",\"chap\":\"4.3.1\",\"knowpointParent\":\"4.3\",\"knpId\":\"4.3.1\",\"knowpName\":\"人民群众是历史创造者\"},{\"description\":\"个人作用的局限性和条件性\",\"knowpExpand\":\"3\",\"chap\":\"4.3.2\",\"knowpointParent\":\"4.3\",\"knpId\":\"4.3.2\",\"knowpName\":\"个人在社会历史中的作用\"},{\"description\":\"相互作用的统一关系\",\"knowpExpand\":\"3\",\"chap\":\"4.3.3\",\"knowpointParent\":\"4.3\",\"knpId\":\"4.3.3\",\"knowpName\":\"群众、阶级、政党、领袖的关系\"},{\"description\":\"\",\"knowpExpand\":\"1\",\"chap\":\"5\",\"knowpointParent\":\"0\",\"knpId\":\"5\",\"knowpName\":\"商品经济与资本主义经济制度\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"5.1\",\"knowpointParent\":\"5\",\"knpId\":\"5.1\",\"knowpName\":\"商品经济的基本问题\"},{\"description\":\"使用价值和价值\",\"knowpExpand\":\"3\",\"chap\":\"5.1.1\",\"knowpointParent\":\"5.1\",\"knpId\":\"5.1.1\",\"knowpName\":\"商品的二因素\"},{\"description\":\"具体劳动和抽象劳动\",\"knowpExpand\":\"3\",\"chap\":\"5.1.2\",\"knowpointParent\":\"5.1\",\"knpId\":\"5.1.2\",\"knowpName\":\"劳动的二重性\"},{\"description\":\"价值规律是商品经济的基本规律\",\"knowpExpand\":\"3\",\"chap\":\"5.1.3\",\"knowpointParent\":\"5.1\",\"knpId\":\"5.1.3\",\"knowpName\":\"价值规律的作用\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"5.2\",\"knowpointParent\":\"5\",\"knpId\":\"5.2\",\"knowpName\":\"资本主义生产方式\"},{\"description\":\"资本主义生产关系的基础\",\"knowpExpand\":\"3\",\"chap\":\"5.2.1\",\"knowpointParent\":\"5.2\",\"knpId\":\"5.2.1\",\"knowpName\":\"劳动力成为商品\"},{\"description\":\"资本家占有工人创造的剩余价值\",\"knowpExpand\":\"3\",\"chap\":\"5.2.2\",\"knowpointParent\":\"5.2\",\"knpId\":\"5.2.2\",\"knowpName\":\"剩余价值的生产\"},{\"description\":\"生产社会化与生产资料私有制的矛盾\",\"knowpExpand\":\"3\",\"chap\":\"5.2.3\",\"knowpointParent\":\"5.2\",\"knpId\":\"5.2.3\",\"knowpName\":\"资本主义基本矛盾\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"5.3\",\"knowpointParent\":\"5\",\"knpId\":\"5.3\",\"knowpName\":\"资本主义的本质与趋势\"},{\"description\":\"资产阶级专政的统治形式\",\"knowpExpand\":\"3\",\"chap\":\"5.3.1\",\"knowpointParent\":\"5.3\",\"knpId\":\"5.3.1\",\"knowpName\":\"政治制度的本质\"},{\"description\":\"为资本主义制度辩护的思想体系\",\"knowpExpand\":\"3\",\"chap\":\"5.3.2\",\"knowpointParent\":\"5.3\",\"knpId\":\"5.3.2\",\"knowpName\":\"意识形态的本质\"},{\"description\":\"由盛到衰的历史必然性\",\"knowpExpand\":\"3\",\"chap\":\"5.3.3\",\"knowpointParent\":\"5.3\",\"knpId\":\"5.3.3\",\"knowpName\":\"资本主义历史地位\"},{\"description\":\"\",\"knowpExpand\":\"1\",\"chap\":\"6\",\"knowpointParent\":\"0\",\"knpId\":\"6\",\"knowpName\":\"社会主义的发展及其规律\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"6.1\",\"knowpointParent\":\"6\",\"knpId\":\"6.1\",\"knowpName\":\"社会主义的历史进程\"},{\"description\":\"科学社会主义理论的建立\",\"knowpExpand\":\"3\",\"chap\":\"6.1.1\",\"knowpointParent\":\"6.1\",\"knpId\":\"6.1.1\",\"knowpName\":\"社会主义从空想到科学\"},{\"description\":\"十月革命实现社会主义制度\",\"knowpExpand\":\"3\",\"chap\":\"6.1.2\",\"knowpointParent\":\"6.1\",\"knpId\":\"6.1.2\",\"knowpName\":\"从理想到现实的发展\"},{\"description\":\"中国社会主义建设的历史成就\",\"knowpExpand\":\"3\",\"chap\":\"6.1.3\",\"knowpointParent\":\"6.1\",\"knpId\":\"6.1.3\",\"knowpName\":\"中国特色社会主义的实践\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"6.2\",\"knowpointParent\":\"6\",\"knpId\":\"6.2\",\"knowpName\":\"科学社会主义基本原则\"},{\"description\":\"无产阶级专政、按劳分配等核心原则\",\"knowpExpand\":\"3\",\"chap\":\"6.2.1\",\"knowpointParent\":\"6.2\",\"knpId\":\"6.2.1\",\"knowpName\":\"基本原则的主要内容\"},{\"description\":\"把握原则的普遍性和具体性\",\"knowpExpand\":\"3\",\"chap\":\"6.2.2\",\"knowpointParent\":\"6.2\",\"knpId\":\"6.2.2\",\"knowpName\":\"正确理解基本原则\"},{\"description\":\"理论与实践的统一\",\"knowpExpand\":\"3\",\"chap\":\"6.2.3\",\"knowpointParent\":\"6.2\",\"knpId\":\"6.2.3\",\"knowpName\":\"与中国特色社会主义结合\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"6.3\",\"knowpointParent\":\"6\",\"knpId\":\"6.3\",\"knowpName\":\"社会主义发展规律\"},{\"description\":\"各国社会主义建设的不同模式\",\"knowpExpand\":\"3\",\"chap\":\"6.3.1\",\"knowpointParent\":\"6.3\",\"knpId\":\"6.3.1\",\"knowpName\":\"发展道路的多样性\"},{\"description\":\"社会主义建设需要长期过程\",\"knowpExpand\":\"3\",\"chap\":\"6.3.2\",\"knowpointParent\":\"6.3\",\"knpId\":\"6.3.2\",\"knowpName\":\"建设过程的长期性\"},{\"description\":\"坚持守正创新的发展道路\",\"knowpExpand\":\"3\",\"chap\":\"6.3.3\",\"knowpointParent\":\"6.3\",\"knpId\":\"6.3.3\",\"knowpName\":\"开拓前进的要求\"},{\"description\":\"\",\"knowpExpand\":\"1\",\"chap\":\"7\",\"knowpointParent\":\"0\",\"knpId\":\"7\",\"knowpName\":\"共产主义崇高理想及其实现\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"7.1\",\"knowpointParent\":\"7\",\"knpId\":\"7.1\",\"knowpName\":\"共产主义社会的基本特征\"},{\"description\":\"物质财富极大丰富\",\"knowpExpand\":\"3\",\"chap\":\"7.1.1\",\"knowpointParent\":\"7.1\",\"knpId\":\"7.1.1\",\"knowpName\":\"社会生产力高度发展\"},{\"description\":\"个人消费品的分配方式\",\"knowpExpand\":\"3\",\"chap\":\"7.1.2\",\"knowpointParent\":\"7.1\",\"knpId\":\"7.1.2\",\"knowpName\":\"按需分配制度\"},{\"description\":\"人的全面自由发展\",\"knowpExpand\":\"3\",\"chap\":\"7.1.3\",\"knowpointParent\":\"7.1\",\"knpId\":\"7.1.3\",\"knowpName\":\"人自由全面发展\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"7.2\",\"knowpointParent\":\"7\",\"knpId\":\"7.2\",\"knowpName\":\"共产主义实现的历史必然性\"},{\"description\":\"资本主义必然被社会主义取代\",\"knowpExpand\":\"3\",\"chap\":\"7.2.1\",\"knowpointParent\":\"7.2\",\"knpId\":\"7.2.1\",\"knowpName\":\"历史发展趋势\"},{\"description\":\"共产主义的实现是一个历史过程\",\"knowpExpand\":\"3\",\"chap\":\"7.2.2\",\"knowpointParent\":\"7.2\",\"knpId\":\"7.2.2\",\"knowpName\":\"实现的长期过程\"},{\"description\":\"在实践中推进共产主义事业\",\"knowpExpand\":\"3\",\"chap\":\"7.2.3\",\"knowpointParent\":\"7.2\",\"knpId\":\"7.2.3\",\"knowpName\":\"实践探索要求\"},{\"description\":\"\",\"knowpExpand\":\"2\",\"chap\":\"7.3\",\"knowpointParent\":\"7\",\"knpId\":\"7.3\",\"knowpName\":\"远大理想与共同理想的关系\"},{\"description\":\"远大理想与共同理想相互关联\",\"knowpExpand\":\"3\",\"chap\":\"7.3.1\",\"knowpointParent\":\"7.3\",\"knpId\":\"7.3.1\",\"knowpName\":\"辩证统一关系\"},{\"description\":\"坚定共产主义远大理想和中国特色社会主义共同理想\",\"knowpExpand\":\"3\",\"chap\":\"7.3.2\",\"knowpointParent\":\"7.3\",\"knpId\":\"7.3.2\",\"knowpName\":\"理想信念要求\"},{\"description\":\"投身新时代中国特色社会主义建设实践\",\"knowpExpand\":\"3\",\"chap\":\"7.3.3\",\"knowpointParent\":\"7.3\",\"knpId\":\"7.3.3\",\"knowpName\":\"新时代的使命担当\"}],\"knowpoint_plan\":\"\"}";
    @Override
    public void afterCreateNewData(EventObject e) {
        FormShowParameter formShowParameter = this.getView().getFormShowParameter();
        Object text = formShowParameter.getCustomParam("paramName");
//        this.getModel().setValue("kdec_textfield", text);
    }
    private boolean IfCourseknpBuilded(){
        // 获取基础资料字段关联的数据包对象
        DynamicObject courseObj = (DynamicObject) this.getModel().getValue("lag1_courseid");
        // 从数据包中提取name属性值
        String courseName = courseObj.getString("name");
        DynamicObject knowpoints = BusinessDataServiceHelper.loadSingle("lag1_d3js_knowpoints", "id,name", new QFilter[]{new QFilter("lag1_course.name", QCP.equals, courseName)});
            return knowpoints==null ? false : true; // 空->对应的知识点方案未被创建->可以创
    }
}
