//package plugins;
//import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
//import kd.bos.entity.plugin.args.BeforeOperationArgs;
//import kd.bos.entity.plugin.args.OperationResult;
//import kd.bos.servicehelper.DispatchServiceHelper;
//import kd.bos.servicehelper.operation.OperationServiceHelper;
//import kd.bos.form.plugin.AbstractFormPlugin;
//import kd.sdk.plugin.Plugin;
//
//import java.awt.event.ActionEvent;
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * 动态表单插件
// */
//public class assistant  extends AbstractOperationServicePlugIn {
//
//        @Override
//        public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
//            // 获取用户输入的信息（假设通过某种方式传递）
//            String userInput = getUserInput(); // 需要实现此方法获取用户输入
//
//            // 调用助手API并传递用户输入
//            callAssistant(userInput);
//
//            // 继续执行默认操作
//        }
//        /**
//         * 获取用户输入的信息
//         * 实现方式取决于您的具体业务场景，例如从表单字段获取
//         */
//        private String getUserInput() {
//            return "你好，你是谁";
//
////            String userInput = this.getModel().getValue("inputField").toString();
////
////            Map<String, String> params = new HashMap<>();
////            params.put("action", "setInput");
////            params.put("text", userInput);
////            params.put("userId", this.getContext().getUserId());
////
////            // 调用助手服务（需替换为实际URL）
////            String response = HttpUtil.post(" https://[您的助手服务域名]/api/assistant/setInput", params);
////            this.getView().showMessage("助手已接收输入");
//        }
//
//        /**
//         * 调用助手API并传递用户输入
//         */
//        private void callAssistant(String userInput) {
//            // 构建传递给助手的参数
//            Map<String, Object> params = new HashMap<>();
//            params.put("userInput", userInput);
//            // 可以根据需要添加更多参数
//
//            // 调用助手服务（假设助手提供了一个接收用户输入的API）
//            // 以下示例使用DispatchServiceHelper调用自定义服务
//            Object response = DispatchServiceHelper.invokeBizService(
//                    "assistantCloudId", // 替换为实际的云ID
//                    "assistantAppId",   // 替换为实际的应用ID
//                    "AssistantService", // 替换为实际的服务名称
//                    "handleUserInput",  // 替换为实际的方法名称
//                    params
//            );
//        }
//
//
//}