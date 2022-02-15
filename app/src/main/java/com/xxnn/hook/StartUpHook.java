package com.xxnn.hook;

import android.content.Context;
import android.os.Build;
import com.xxnn.utils.Initiator;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author weiguan
 * @desc:
 * @date 2022/2/15 11:14
 */
public class StartUpHook {
    public static final String MC_FULL_TAG = "mc_full_tag";
    private boolean first_stage_inited = false;
    private static boolean sec_static_stage_inited = false;
    public static StartUpHook SELF;

    public static StartUpHook getInstance() {
        if (SELF == null) {
            SELF = new StartUpHook();
        }
        return SELF;
    }

    public void doInit(ClassLoader rtLoader) throws Throwable {
        if (first_stage_inited) {
            return;
        }
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
                        if (app != null) {
                            XposedBridge.log("McHookTool: 注入成功");
                        }
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
            first_stage_inited = true;
        } catch (Throwable e) {
            if ((e + "").contains("com.bug.zqq")) {
                return;
            }
            if ((e + "").contains("com.google.android.webview")) {
                return;
            }
            throw e;
        }
        try {
            XposedHelpers
                    .findAndHookMethod(rtLoader.loadClass("com.tencent.mobileqq.qfix.QFixApplication"),
                            "attachBaseContext", Context.class, new XC_MethodHook() {
                                @Override
                                public void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                    deleteDirIfNecessaryNoThrow((Context) param.args[0]);
                                }
                            });
        } catch (ClassNotFoundException ignored) {
        }
    }

    public static void execStartupInit(Context ctx, Object step, String lpwReserved,
                                       boolean bReserved) throws Throwable {
        if (sec_static_stage_inited) {
            return;
        }
        XposedBridge.log("McHookTool: 进入可执行状态");
        ClassLoader classLoader = ctx.getClassLoader();
        if (classLoader == null) {
            throw new AssertionError("ERROR: classLoader == null");
        }
        if ("true".equals(System.getProperty(MC_FULL_TAG))) {
            XposedBridge.log("Err:McHookTool reloaded??");
            //I don't know... What happened?
            return;
        }
        System.setProperty(MC_FULL_TAG, "true");
        injectClassLoader(classLoader);
        Initiator.init(ctx.getClassLoader());

        MainHook.getInstance().hookMethod(classLoader);
        sec_static_stage_inited = true;
        deleteDirIfNecessaryNoThrow(ctx);
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
            if (!curr.getClass().getName().equals(HybridClassLoader.class.getName())) {
                fParent.set(mine, new HybridClassLoader(curr, classLoader));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void deleteDirIfNecessaryNoThrow(Context ctx) {
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                deleteFile(new File(ctx.getDataDir(), "app_qqprotect"));
            }
            if (new File(ctx.getFilesDir(), "qn_disable_hot_patch").exists()) {
                deleteFile(ctx.getFileStreamPath("hotpatch"));
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static boolean deleteFile(File file) {
        if (!file.exists()) {
            return false;
        }
        if (file.isFile()) {
            file.delete();
        } else if (file.isDirectory()) {
            File[] listFiles = file.listFiles();
            if (listFiles != null) {
                for (File deleteFile : listFiles) {
                    deleteFile(deleteFile);
                }
            }
            file.delete();
        }
        return !file.exists();
    }
}
