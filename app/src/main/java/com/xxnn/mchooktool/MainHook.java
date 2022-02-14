package com.xxnn.mchooktool;

import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author weiguan
 * @desc:
 * @date 2022/2/14 16:06
 */
public class MainHook implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 过滤包名
        if (!"com.tencent.mobileqq".equals(lpparam.packageName)) {
            return;
        }

        try {
            // 开始hook方法
            // 参数1: class路径, 参数3: 方法名
            XposedHelpers.findAndHookMethod("com.tencent.qphone.base.util.CodecWarpper",
                    lpparam.classLoader, "getInfo", new XC_MethodHook() {
                        // 执行方法之前执行的方法
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                        }

                        // 执行方法之后执行的方法
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                        }
                    });
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}
