package com.xxnn.mchooktool;


import com.xxnn.hook.StartUpHook;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @author weiguan
 * @desc:
 * @date 2022/2/14 16:06
 */
public class HookEntry implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // 过滤包名
        if (!"com.tencent.mobileqq".equals(lpparam.packageName)) {
            return;
        }

        try {
            // 开始初始化
            StartUpHook.getInstance().doInit(lpparam.classLoader);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }
}
