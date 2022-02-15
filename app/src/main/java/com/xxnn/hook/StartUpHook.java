package com.xxnn.hook;

import android.content.Context;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author weiguan
 * @desc:
 * @date 2022/2/15 11:14
 */
public class StartUpHook {
    private static boolean sec_static_stage_inited = false;
    public static StartUpHook SELF;

    public static StartUpHook getInstance() {
        if (SELF == null) {
            SELF = new StartUpHook();
        }
        return SELF;
    }

    public void doInit(ClassLoader rtLoader) throws Throwable {
        try {
            XC_MethodHook startup = new XC_MethodHook(51) {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Context app;
                        Class<?> clz = param.thisObject.getClass().getClassLoader()
                                .loadClass("com.tencent.common.app.BaseApplicationImpl");
                        Field fsApp = null;
                        for (Field f : clz.getDeclaredFields()) {
                            if (f.getType() == clz) {
                                fsApp = f;
                                break;
                            }
                        }
                        if (fsApp == null) {
                            throw new NoSuchFieldException(
                                    "field BaseApplicationImpl.sApplication not found");
                        }
                        app = (Context) fsApp.get(null);
                        execStartupInit(app, param.thisObject, null, false);
                    } catch (Throwable e) {
                        throw e;
                    }
                }
            };
            Class<?> loadDex = rtLoader.loadClass("com.tencent.mobileqq.startup.step.LoadDex");
            Method[] ms = loadDex.getDeclaredMethods();
            Method m = null;
            for (Method method : ms) {
                if (method.getReturnType().equals(boolean.class)
                        && method.getParameterTypes().length == 0) {
                    m = method;
                    break;
                }
            }
            XposedBridge.hookMethod(m, startup);
            sec_static_stage_inited = true;
        } catch (Throwable e) {
            if ((e + "").contains("com.bug.zqq")) {
                return;
            }
            if ((e + "").contains("com.google.android.webview")) {
                return;
            }
            throw e;
        }
    }

    public static void execStartupInit(Context ctx, Object step, String lpwReserved,
                                       boolean bReserved) throws Throwable {
        if (sec_static_stage_inited) {
            return;
        }
        ClassLoader classLoader = ctx.getClassLoader();
        if (classLoader == null) {
            throw new AssertionError("ERROR: classLoader == null");
        }
        injectClassLoader(classLoader);
        MainHook.getInstance().hookMethod(classLoader);
        sec_static_stage_inited = true;
    }

    private static void injectClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader == null");
        }
        try {
            Field fParent = ClassLoader.class.getDeclaredField("parent");
            fParent.setAccessible(true);
            ClassLoader mine = StartUpHook.class.getClassLoader();
            ClassLoader curr = (ClassLoader) fParent.get(mine);
            if (curr == null) {
                curr = XposedBridge.class.getClassLoader();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
