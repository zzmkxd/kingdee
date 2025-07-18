package plugins.test;

/**
 * @说明 TODO
 * @Author Tian_Yumi
 * @Date 2025/7/16 11:29
 * @Version 1.0
 */

import okhttp3.*;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class BaiduFaceForKd extends JFrame {

    /* ====== 与 BaiduFaceGUI 保持一致 ====== */
    private static final String APP_ID     = "119488107";
    private static final String API_KEY    = "Z62REkMyAM01FaKfxPKJ69lC";
    private static final String SECRET_KEY = "VMHn58zyuw8T4veynd38QbUmDdyAYUx2";
    /* ====================================== */

    private final OkHttpClient client = new OkHttpClient();
    private String accessToken;

    private final JLabel camLabel = new JLabel();
    private final JButton btnSnap = new JButton("确认拍照");

    private FrameGrabber grabber;
    private volatile boolean running = true;

    private volatile String apiResult;   // 保存 API 结果
    private volatile Action nextAction;  // 下一步动作
    private String groupIdCache, userIdCache, userInfoCache, qualityCache;

    private enum Action { REGISTER, RECOGNIZE, DELETE }

    /* ========= 构造 ========= */
    public BaiduFaceForKd() {
        super("百度人脸识别 - 拍照确认");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(640, 480);
        setLocationRelativeTo(null);

        /* 摄像头显示区 */
        add(camLabel, BorderLayout.CENTER);

        /* 底部按钮 */
        JPanel bottom = new JPanel();
        bottom.add(btnSnap);
        add(bottom, BorderLayout.SOUTH);

        btnSnap.addActionListener(e -> snapAndRun());

        /* 空格键拍照 */
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "snap");
        getRootPane().getActionMap().put("snap", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { snapAndRun(); }
        });

        initToken();
        startCamera();
    }

    /* ========= 摄像头线程 ========= */
    private void startCamera() {
        Thread camThread = new Thread(() -> {
            try {
                grabber = FrameGrabber.createDefault(0);
                grabber.setImageWidth(640);
                grabber.setImageHeight(480);
                grabber.start();
                while (running) {
                    Frame frame = grabber.grab();
                    if (!running) break;              // 多一次保护
                    BufferedImage img = convertFrameToBufferedImage(frame);
                    if (img != null) {
                        ImageIcon icon = new ImageIcon(img);
                        SwingUtilities.invokeLater(() -> camLabel.setIcon(icon));
                    }
                }
            } catch (FrameGrabber.Exception ignored) {
                // 正常收尾时 grab 会抛异常，直接忽略
            } catch (Exception e) {
                log("摄像头异常: " + e.getMessage());
            } finally {
                try { if (grabber != null) grabber.release(); } catch (Exception ignored) {}
            }
        });
        camThread.setDaemon(true);   // 主线程退出时自动结束
        camThread.start();
    }

    private BufferedImage convertFrameToBufferedImage(Frame frame) {
        return new Java2DFrameConverter().convert(frame);
    }

    /* ========= 对外入口 ========= */
    public String doRegister(String group_id, String user_id,
                             String user_info, String quality_control) {
        setParams(group_id, user_id, user_info, quality_control);
        nextAction = Action.REGISTER;
        return showAndWait();
    }

    public String doRecognize(String group_id) {
        setParams(group_id, null, null, null);
        nextAction = Action.RECOGNIZE;
        return showAndWait();
    }

    public String doDelete(String group_id, String user_id) {
        setParams(group_id, user_id, null, null);
        nextAction = Action.DELETE;
        return showAndWait();
    }

    /* 保存参数并显示窗口 */
    private void setParams(String gid, String uid, String info, String qc) {
        groupIdCache  = gid;
        userIdCache   = uid;
        userInfoCache = info;
        qualityCache  = qc;
        apiResult     = null;
    }

    private String showAndWait() {
        SwingUtilities.invokeLater(() -> {
            setVisible(true);
            toFront();
        });
        while (apiResult == null) {
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        }
        return apiResult;
    }

    /* ========= 拍照并调用对应 API ========= */
    private void snapAndRun() {
        try {
            BufferedImage snap = convertFrameToBufferedImage(grabber.grab());
            if (snap == null) return;
            String base64 = imgToBase64(snap);

            switch (nextAction) {
                case REGISTER:
                    callApi("https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add",
                            new JSONObject()
                                    .put("image", base64)
                                    .put("image_type", "BASE64")
                                    .put("group_id", groupIdCache)
                                    .put("user_id", userIdCache)
                                    .put("user_info", userInfoCache)
                                    .put("quality_control", qualityCache)
                                    .put("action_type", "REPLACE")
                    );
                    break;
                case RECOGNIZE:
                    callApi("https://aip.baidubce.com/rest/2.0/face/v3/search",
                            new JSONObject()
                                    .put("image", base64)
                                    .put("image_type", "BASE64")
                                    .put("group_id_list", groupIdCache)
                                    .put("max_user_num", 1)
                    );
                    break;
                case DELETE:
                    callApi("https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/delete",
                            new JSONObject()
                                    .put("group_id", groupIdCache)
                                    .put("user_id", userIdCache)
                    );
                    break;
            }
        } catch (Exception e) {
            log("抓拍异常: " + e.getMessage());
        }
    }

    /* ========= 获取 access_token ========= */
    private void initToken() {
        new Thread(() -> {
            try {
                String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials"
                        + "&client_id=" + API_KEY + "&client_secret=" + SECRET_KEY;
                Request req = new Request.Builder().url(url).get().build();
                Response resp = client.newCall(req).execute();
                accessToken = new JSONObject(resp.body().string()).getString("access_token");
            } catch (Exception e) {
                log("Token 失败: " + e.getMessage());
            }
        }).start();
    }

    /* ========= 统一 API 调用 ========= */
    private void callApi(String url, JSONObject body) {
        new Thread(() -> {
            try {
                String full = url + "?access_token=" + accessToken;
                RequestBody reqBody = RequestBody.create(
                        body.toString(), MediaType.parse("application/json; charset=utf-8"));
                Request req = new Request.Builder().url(full).post(reqBody).build();
                Response resp = client.newCall(req).execute();
                apiResult = resp.body().string();
                log(apiResult);
                SwingUtilities.invokeLater(this::dispose);
            } catch (Exception e) {
                log("API 异常: " + e.getMessage());
            }
        }).start();
    }

    /* ========= 工具 ========= */
    private String imgToBase64(BufferedImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private void log(String msg) {
        System.out.println(msg); // 控制台打印
    }

    @Override
    public void dispose() {
        running = false;
        try { if (grabber != null) grabber.stop(); } catch (Exception ignored) {}
        super.dispose();
    }

    /* ========= 测试用 main ========= */
    public static void main(String[] args) {
        BaiduFaceForKd kd = new BaiduFaceForKd();
        // 示例：注册
//        String res = kd.doRegister("group_test", "user_114514", "小明", "NORMAL");
        String res = kd.doRecognize("group_test");
//        String res = kd.doDelete("group_test", "user_114514");

        System.out.println("注册结果：" + res);
    }
}