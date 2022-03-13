package com.xxnn.hook;

import android.annotation.SuppressLint;
import android.os.Environment;
import com.xxnn.data.HostInfo;
import com.xxnn.utils.HttpUtil;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import okhttp3.*;

import java.io.*;

import static com.xxnn.utils.Initiator.load;

/**
 * @author weiguan
 * @desc:
 * @date 2022/2/15 11:16
 */
public class MainHook {
    private static boolean isInit = false;
    public static MainHook SELF;
    private String open;
    private String address;

    public MainHook() {
        try {
            String path = HostInfo.getInstance().getContext().getExternalFilesDir("").getAbsolutePath();
            FileReader fileReader = new FileReader(path + "/mchook/open.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            open = bufferedReader.readLine();
            XposedBridge.log("mcHookTool: 状态: " + open);
            bufferedReader.close();
            fileReader.close();

            fileReader = new FileReader(path + "/mchook/address.txt");
            bufferedReader = new BufferedReader(fileReader);
            address = bufferedReader.readLine();
            XposedBridge.log("mcHookTool: 地址: " + address);
            bufferedReader.close();
            fileReader.close();
        } catch (IOException e) {
            XposedBridge.log("mcHookTool: 读取文件出错" + e.getMessage());
        }
    }

    public static MainHook getInstance() {
        if (SELF == null) {
            SELF = new MainHook();
        }
        return SELF;
    }


    public void hookMethod(ClassLoader classLoader) {
        Class clz = load("com.tencent.qphone.base.util.CodecWarpper");
        if (clz == null) {
            XposedBridge.log("McHookTool: CodecWarpper isnull");
        }
        hookFirst(clz);
    }

    private void hookFirst(Class<?> clazz) {
        XC_MethodHook init = new XC_MethodHook() {
            // 执行方法之前执行的方法
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // hook初始化函数, 强制打开调试模式
                if (param.args.length >= 2) {
                    param.args[1] = true;
                    if (!isInit) {
                        hookReceivePacket(param.thisObject.getClass());
                        isInit = true;
                    }
                }
            }
        };
        XC_MethodHook onReceData = new XC_MethodHook() {
            // 执行方法之后执行的方法
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!isInit) {
                    hookReceivePacket(param.thisObject.getClass());
                    isInit = true;
                }
            }
        };
        XC_MethodHook encodeRequest = new XC_MethodHook() {
            // 执行方法之后执行的方法
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                hookSendPacket(param);
            }
        };
        XposedBridge.hookAllMethods(clazz, "init", init);
        XposedBridge.hookAllMethods(clazz, "onReceData", onReceData);
        XposedBridge.hookAllMethods(clazz, "encodeRequest", encodeRequest);
    }


    private void hookSendPacket(XC_MethodHook.MethodHookParam param) {
        if (open.equals("f")) {
            return;
        }
        // encode结果, 字节数组
        byte[] result = (byte[]) param.getResult();
        if (param.args.length == 17) {
            Integer seq = (Integer) param.args[0];
            String command = (String) param.args[5];
            String uin = (String) param.args[9];
            byte[] buffer = (byte[]) param.args[15];
            saveRequest(seq, command, uin, buffer);
        } else if (param.args.length == 16) {
            Integer seq = (Integer) param.args[0];
            String command = (String) param.args[5];
            String uin = (String) param.args[9];
            byte[] buffer = (byte[]) param.args[14];
            saveRequest(seq, command, uin, buffer);
        } else if (param.args.length == 14) {
            Integer seq = (Integer) param.args[0];
            String command = (String) param.args[5];
            String uin = (String) param.args[9];
            byte[] buffer = (byte[]) param.args[12];
            saveRequest(seq, command, uin, buffer);
        } else {
            XposedBridge.log("McHookTool -> send: hook到了个不知道什么东西");
        }
    }

    private void hookReceivePacket(Class<?> clazz) {
        XC_MethodHook xcMethodHook = new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (open.equals("f")) {
                    return;
                }
                Object object = param.args[1];
                byte[] buffer = (byte[]) XposedHelpers.callMethod(object, "getWupBuffer");
                String command = (String) XposedHelpers.callMethod(object, "getServiceCmd");
                String uin = (String) XposedHelpers.callMethod(object, "getUin");
                Integer ssoSeq = (Integer) XposedHelpers.callMethod(object, "getRequestSsoSeq");
                // byte[] msgCookie = (byte[]) XposedHelpers.callMethod(object, "getMsgCookie");
                saveReceive(ssoSeq, command, uin, buffer);
            }
        };
        XposedBridge.hookAllMethods(clazz, "onResponse", xcMethodHook);
    }

    private void saveRequest(Integer seq, String command, String uin, byte[] buffer) {
        if (address == null || "".equals(address)) {
            return;
        }
        if (buffer.length == 0) {
            buffer = new byte[]{1};
        }
        String url = String.format(address + "/send?seq=%s&command=%s&uin=%s", seq, command, uin);
        HttpUtil.post(url, buffer);
    }

    private void saveReceive(Integer seq, String command, String uin, byte[] buffer) {
        if (address == null || "".equals(address)) {
            return;
        }
        if (buffer.length == 0) {
            buffer = new byte[]{1};
        }
        String url = String.format(address + "/receive?seq=%s&command=%s&uin=%s", seq, command, uin);
        HttpUtil.post(url, buffer);
    }
}
