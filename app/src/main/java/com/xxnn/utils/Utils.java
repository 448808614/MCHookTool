/*
 * QNotified - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2022 dmca@ioctl.cc
 * https://github.com/ferredoxin/QNotified
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by ferredoxin.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/ferredoxin/QNotified/blob/master/LICENSE.md>.
 */
package com.xxnn.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import de.robv.android.xposed.XposedBridge;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.xxnn.utils.Initiator.load;
import static com.xxnn.utils.ReflexUtil.invoke_virtual;
import static de.robv.android.xposed.XposedBridge.log;


@SuppressLint("SimpleDateFormat")
public class Utils {

    public static final boolean __REMOVE_PREVIOUS_CACHE = false;
    public static final String PACKAGE_NAME_QQ = "com.tencent.mobileqq";
    public static final String PACKAGE_NAME_QQ_INTERNATIONAL = "com.tencent.mobileqqi";
    public static final String PACKAGE_NAME_QQ_LITE = "com.tencent.qqlite";
    public static final String PACKAGE_NAME_TIM = "com.tencent.tim";
    public static final String PACKAGE_NAME_SELF = "nil.nadph.qnotified";
    public static final String PACKAGE_NAME_XPOSED_INSTALLER = "de.robv.android.xposed.installer";
    public static final int TOAST_TYPE_INFO = 0;
    public static final int TOAST_TYPE_ERROR = 1;
    public static final int TOAST_TYPE_SUCCESS = 2;
    public static boolean ENABLE_DUMP_LOG = false;
    private static Handler mHandler;
    private static boolean sAppRuntimeInit = false;
    private static Field f_mAppRuntime = null;

    private Utils() {
        throw new AssertionError("No instance for you!");
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.replace(" ", "").equalsIgnoreCase("");
    }

    public static void runOnUiThread(Runnable r) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            r.run();
        } else {
            if (mHandler == null) {
                mHandler = new Handler(Looper.getMainLooper());
            }
            mHandler.post(r);
        }
    }


    public static boolean isCallingFrom(String classname) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            if (element.toString().contains(classname)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCallingFromEither(String... classname) {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            for (String name : classname) {
                if (element.toString().contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }


    public static String paramsTypesToString(Class... c) {
        if (c == null) {
            return null;
        }
        if (c.length == 0) {
            return "()";
        }
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < c.length; i++) {
            sb.append(c[i] == null ? "[null]" : c[i].getName());
            if (i != c.length - 1) {
                sb.append(",");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    public static String get_RGB(int color) {
        int r = 0xff & (color >> 16);
        int g = 0xff & (color >> 8);
        int b = 0xff & color;
        return "#" + byteStr(r) + byteStr(g) + byteStr(b);
    }

    public static String byteStr(int i) {
        String ret = Integer.toHexString(i);
        if (ret.length() == 1) {
            return "0" + ret;
        } else {
            return ret;
        }
    }

    public static void $access$set$sAppRuntimeInit(boolean z) {
        sAppRuntimeInit = z;
    }


    public static void logi(String str) {
        try {
            Log.i("QNdump", str);
            log(str);
        } catch (NoClassDefFoundError e) {
            Log.i("Xposed", str);
            Log.i("EdXposed-Bridge", str);
        }
        if (ENABLE_DUMP_LOG) {
            String path =
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/qn_log.txt";
            File f = new File(path);
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
                appendToFile(path,
                        "[" + System.currentTimeMillis() + "-" + android.os.Process.myPid() + "] " + str
                                + "\n");
            } catch (IOException e) {
            }
        }
    }

    public static void logw(String str) {
        Log.i("QNdump", str);
        try {
            log(str);
        } catch (NoClassDefFoundError e) {
            Log.w("Xposed", str);
            Log.w("EdXposed-Bridge", str);
        }
        if (ENABLE_DUMP_LOG) {
            String path =
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/qn_log.txt";
            File f = new File(path);
            try {
                if (!f.exists()) {
                    f.createNewFile();
                }
                appendToFile(path,
                        "[" + System.currentTimeMillis() + "-" + android.os.Process.myPid() + "] " + str
                                + "\n");
            } catch (IOException e) {
            }
        }
    }

    public static void checkLogFlag() {
        try {
            File f = new File(
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/.qn_log_flag");
            if (f.exists()) {
                ENABLE_DUMP_LOG = true;
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * 追加文件：使用FileWriter 不能使用LogAndToastUtil.log(Throwable),防止死递归
     *
     * @param fileName
     * @param content
     */
    public static void appendToFile(String fileName, String content) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(fileName, true);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String en(String str) {
        if (str == null) {
            return "null";
        }
        return "\"" + str.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    public static String de(String str) {
        if (str == null) {
            return null;
        }
        if (str.equals("null")) {
            return null;
        }
        if (str.startsWith("\"")) {
            str = str.substring(1);
        }
        if (str.endsWith("\"") && !str.endsWith("\\\"")) {
            str = str.substring(0, str.length() - 1);
        }
        return str.replace("\\\"", "\"").replace("\\\n", "\n")
                .replace("\\\r", "\r").replace("\\\\", "\\");
    }

    public static String csvenc(String s) {
        if (!s.contains("\"") && !s.contains(" ") && !s.contains(",") && !s.contains("\r") && !s
                .contains("\n") && !s.contains("\t")) {
            return s;
        }
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private static boolean isSymbol(char c) {
        if (c == '\u3000') {
            return true;
        }
        if (c < '0') {
            return true;
        }
        if (c > '9' && c < 'A') {
            return true;
        }
        if (c > 'Z' && c < 'a') {
            return true;
        }
        return (c <= 0xD7FF);
    }

    /**
     * 通过view暴力获取getContext()(Android不支持view.getContext()了)
     *
     * @param view 要获取context的view
     * @return 返回一个activity
     */
    public static Context getContext(View view) {
        Context ctx = null;
        if (view.getContext().getClass().getName()
                .contains("com.android.internal.policy.DecorContext")) {
            try {
                Field field = view.getContext().getClass().getDeclaredField("mPhoneWindow");
                field.setAccessible(true);
                Object obj = field.get(view.getContext());
                Method m1 = obj.getClass().getMethod("getContext");
                ctx = (Context) (m1.invoke(obj));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            ctx = view.getContext();
        }
        return ctx;
    }

    public static String getShort$Name(Object obj) {
        String name;
        if (obj == null) {
            return "null";
        }
        if (obj instanceof String) {
            name = ((String) obj).replace("/", ".");
        } else if (obj instanceof Class) {
            name = ((Class) obj).getName();
        } else if (obj instanceof Field) {
            name = ((Field) obj).getType().getName();
        } else {
            name = obj.getClass().getName();
        }
        if (!name.contains(".")) {
            return name;
        }
        int p = name.lastIndexOf('.');
        return name.substring(p + 1);
    }


    /**
     * 根据手机的分辨率从 dip 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static float dip2sp(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density /
                context.getResources().getDisplayMetrics().scaledDensity;
        return dpValue * scale + 0.5f;
    }

    /**
     * 将px值转换为sp值，保证文字大小不变
     */
    public static int px2sp(Context context, float pxValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static void copy(File s, File f) throws Exception {
        if (!s.exists()) {
            throw new FileNotFoundException("源文件不存在");
        }
        if (!f.exists()) {
            f.createNewFile();
        }
        FileReader fr = new FileReader(s);
        FileWriter fw = new FileWriter(f);
        char[] buff = new char[1024];
        for (int len = 0; len != -1; len = fr.read(buff)) {
            fw.write(buff, 0, len);
        }
        fw.close();
        fr.close();
    }

    public static String en_toStr(Object obj) {
        if (obj == null) {
            return null;
        }
        String str;
        if (obj instanceof CharSequence) {
            str = Utils.en(obj.toString());
        } else {
            str = "" + obj;
        }
        return str;
    }

    public static Activity getCurrentActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread")
                    .invoke(null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            log(e);
        }
        return null;
    }

    @SuppressWarnings("JavaJniMissingFunction")
    private static native long ntGetBuildTimestamp();

    public static int strcmp(String stra, String strb) {
        int len = Math.min(stra.length(), strb.length());
        for (int i = 0; i < len; i++) {
            char a = stra.charAt(i);
            char b = strb.charAt(i);
            if (a != b) {
                return a - b;
            }
        }
        return stra.length() - strb.length();
    }

    public static int[] integerSetToArray(Set<Integer> is) {
        int[] ret = new int[is.size()];
        Iterator<Integer> it = is.iterator();
        for (int i = 0; i < ret.length; i++) {
            if (it.hasNext()) {
                ret[i] = it.next();
            }
        }
        return ret;
    }

    public static String getPathTail(File path) {
        return getPathTail(path.getPath());
    }

    public static String getPathTail(String path) {
        String[] arr = path.split("/");
        return arr[arr.length - 1];
    }

    public static String getFileContent(InputStream in) throws IOException {
        if (in == null) {
            throw new NullPointerException("InputStream is null");
        }
        BufferedReader br = null;
        StringBuffer sb;
        try {
            br = new BufferedReader(new InputStreamReader(in));
            sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static String getFileContent(String path) throws IOException {
        return getFileContent(new FileInputStream(path));
    }

    public static void saveFileContent(String path, String content) throws IOException {
        FileOutputStream fout = new FileOutputStream(path);
        fout.write(content.getBytes());
        fout.flush();
        fout.close();
    }

    public static class DummyCallback implements DialogInterface.OnClickListener {

        public DummyCallback() {
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
        }

    }

    public static class ContactDescriptor {

        public String uin;
        public int uinType;
        @Nullable
        public String nick;

        public String getId() {
            StringBuilder msg = new StringBuilder();
            if (uin.length() < 10) {
                for (int i = 0; i < 10 - uin.length(); i++) {
                    msg.append("0");
                }
            }
            return msg + uin + uinType;
        }
    }

    public static File makeFilePath(String filePath, String fileName) throws IOException {
        File file = null;
        makeRootDirectory(filePath);
        file = new File(filePath + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        file = new File(filePath);
        if (!file.exists()) {
            file.mkdir();
        }
    }
}
