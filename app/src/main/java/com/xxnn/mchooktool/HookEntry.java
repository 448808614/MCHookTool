package com.xxnn.mchooktool;

import android.content.Context;
import android.util.Log;
import com.xxnn.hook.StartUpHook;
import de.robv.android.xposed.*;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
            StartUpHook.getInstance().doInit(lpparam.classLoader);
        } catch (Throwable e) {
            XposedBridge.log(e);
        }
    }




}
