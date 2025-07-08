package plugins.question;

import kd.bos.schedule.executor.AbstractTask;
import kd.bos.workflow.api.AgentExecution;
import kd.sdk.plugin.Plugin;
import com.alibaba.druid.util.StringUtils;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.exception.KDException;
import kd.bos.orm.query.QFilter;
import kd.bos.schedule.executor.AbstractTask;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.servicehelper.user.UserServiceHelper;
import kd.bos.servicehelper.workflow.MessageCenterServiceHelper;
import kd.bos.workflow.engine.msg.info.MessageInfo;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 后台任务插件
 */
public class CheckDDL extends AbstractTask implements Plugin {

        @Override
        public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
            // 获取作业的预定信息
//            String fields = "number,myg6_basedatafield,myg6_basedatafield_seat,myg6_timefield_end";
            QFilter[] filters = new QFilter[0];
            DynamicObject[] dys = BusinessDataServiceHelper.load("lag1_problems","*", filters);

            String info = "这个作业即将逾期了：\n";

            for (DynamicObject single : dys) {
                // 获取该作业名称
                DynamicObject seat = (DynamicObject) single.get("lag1_problems");
                Date done_time = single.getDate("lag1_ddl");
                // 获取当前日期
                Date currentDate = new Date();
                // 计算前一天日期
                Date targetDate = calculateNextDay(currentDate);

                int result = done_time.compareTo(targetDate);
                if (result > 0) {
                    System.out.println("ddl晚于当前日期的后一天");
                    continue;
                }
//                else {
//                    System.out.println("该提醒了");
//                }
                String problemsNumber = seat.getString("number");
                System.out.println("problemsID info:" + problemsNumber);
                // 添加信息
                info += problemsNumber + "\n";

            }
            // 如果当前没有作业逾期，则直接返回
            if (info.equals("这些作业已经逾期了：\n")) return;

            // 开始发送站内消息
            MessageInfo messageInfo = new MessageInfo();
            LocaleString title = new LocaleString();
            title.setLocaleValue_zh_CN("作业逾期提示");
            messageInfo.setMessageTitle(title);
            LocaleString content = new LocaleString();
            content.setLocaleValue_zh_CN(info);
            messageInfo.setMessageContent(content);

            //获取当前的业务单元
            long orgId = requestContext.getOrgId();

            // 获取业务单元中所有的用户
            List<Long> allUsersOfOrg = UserServiceHelper.getAllUsersOfOrg(orgId);
            ArrayList<Long> ids = new ArrayList<Long>();

            // 获取当前登录用户id
            ids.add(requestContext.getCurrUserId());
            messageInfo.setUserIds(ids);
            messageInfo.setType(MessageInfo.TYPE_MESSAGE);
            messageInfo.setTag("作业逾期");

            // 发送消息
            MessageCenterServiceHelper.sendMessage(messageInfo);
        }

        /**
         * 计算当前日期的前一天
         *
         * @param date 当前日期
         * @return 前一天日期
         */
        private Date calculateNextDay(Date date) {
            // 使用Calendar或Java 8日期时间API进行日期计算
            // 这里使用Calendar作为示例
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(date);
            cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            return cal.getTime();
        }
}