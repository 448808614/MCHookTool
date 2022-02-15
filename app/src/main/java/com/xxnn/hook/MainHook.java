package com.xxnn.hook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author weiguan
 * @desc:
 * @date 2022/2/15 11:16
 */
public class MainHook {
    private static boolean isInit = false;
    public static MainHook SELF;

    public static MainHook getInstance() {
        if (SELF == null) {
            SELF = new MainHook();
        }
        return SELF;
    }


    public void hookMethod(ClassLoader classLoader) {
        // 开始hook方法
        // 参数1: class路径, 参数3: 方法名

        // hook初始化函数, 强制打开调试模式
        XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.CodecWarpper",
                classLoader, "init", new XC_MethodHook() {
                    // 执行方法之前执行的方法
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args.length >= 2) {
                            param.args[1] = true;
                            if (!isInit) {
                                hookReceivePacket(param.thisObject.getClass());
                                isInit = true;
                            }
                        }
                    }
                });

        // hook接收的消息
        XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.CodecWarpper",
                classLoader, "onReceData", new XC_MethodHook() {
                    // 执行方法之后执行的方法
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (!isInit) {
                            hookReceivePacket(param.thisObject.getClass());
                            isInit = true;
                        }
                    }
                });

        // hook收到的消息
        XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.CodecWarpper",
                classLoader, "encodeRequest", new XC_MethodHook() {
                    // 执行方法之后执行的方法
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        hookSendPacket(param);
                    }
                });
    }


    private void hookSendPacket(XC_MethodHook.MethodHookParam param) {
        // encode结果, 字节数组
        byte[] result = (byte[]) param.getResult();
        if (param.args.length == 17) {
            Integer seq = (Integer) param.args[0];
            String command = (String) param.args[5];
            String uin = (String) param.args[9];
            byte[] buffer = (byte[]) param.args[15];
            XposedBridge.log("McHookTool: " + command);
            saveData(command);
        } else if (param.args.length == 16) {
            Integer seq = (Integer) param.args[0];
            String command = (String) param.args[5];
            String uin = (String) param.args[9];
            byte[] buffer = (byte[]) param.args[14];
            XposedBridge.log("McHookTool: " + command);
            saveData(command);
        } else if (param.args.length == 14) {
            Integer seq = (Integer) param.args[0];
            String command = (String) param.args[5];
            String uin = (String) param.args[9];
            byte[] buffer = (byte[]) param.args[12];
            XposedBridge.log("McHookTool: " + command);
            saveData(command);
        } else {
            XposedBridge.log("McHookTool: hook到了个不知道什么东西");
        }
    }

    private void hookReceivePacket(Class<?> clazz) {
        XC_MethodHook xcMethodHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                return;
            }
        };
        XposedBridge.hookAllMethods(clazz, "onResponse", xcMethodHook);
    }

    private void saveData(String command) {
        String url = String.format("http://192.168.8.58:8888/test/receive?command=%s", command);
        OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url)
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {

            }
        });
    }
}
