package com.xxnn.mchooktool;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import java.io.ByteArrayInputStream;

/**
 * @author weiguan
 * @desc:
 * @date 2022/2/14 16:06
 */
public class MainHook implements IXposedHookLoadPackage {
    private static boolean isInit = false;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 过滤包名
        if (!"com.tencent.mobileqq".equals(lpparam.packageName)) {
            return;
        }

        try {
            // 开始hook方法
            // 参数1: class路径, 参数3: 方法名

            // hook初始化函数, 强制打开调试模式
            XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.CodecWarpper",
                    lpparam.classLoader, "init", new XC_MethodHook() {
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
                    lpparam.classLoader, "onReceData", new XC_MethodHook() {
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
                    lpparam.classLoader, "encodeRequest", new XC_MethodHook() {
                        // 执行方法之后执行的方法
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            hookSendPacket(param);
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }

    private void hookSendPacket(XC_MethodHook.MethodHookParam param) {
        // encode结果, 字节数组
        byte[] result = (byte[]) param.getResult();
        if (param.args.length == 17) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(result);
        } else if (param.args.length == 16) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(result);
        } else if (param.args.length == 14) {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(result);
        } else {
            XposedBridge.log("hook到了个不知道什么东西");
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
}
