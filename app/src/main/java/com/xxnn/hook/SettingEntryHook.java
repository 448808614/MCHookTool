package com.xxnn.hook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import com.xxnn.mchooktool.MainActivity;
import com.xxnn.mchooktool.R;
import com.xxnn.utils.ReflexUtil;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.xxnn.utils.Initiator.load;
import static com.xxnn.utils.ReflexUtil.*;
import static de.robv.android.xposed.XposedBridge.log;


/**
 * @author Ad
 */
public class SettingEntryHook {
    public static final SettingEntryHook INSTANCE = new SettingEntryHook();

    public boolean initOnce() {
        try {
            XposedHelpers
                    .findAndHookMethod(load("com.tencent.mobileqq.activity.QQSettingSettingActivity"),
                            "doOnCreate", Bundle.class, new XC_MethodHook(52) {
                                @Override
                                protected void afterHookedMethod(final MethodHookParam param) throws Throwable {
                                    try {
                                        XposedBridge.log("[mcHookTool] -> 开始增加列表按钮");
                                        final Activity activity = (Activity) param.thisObject;
                                        Class<?> itemClass;
                                        View itemRef;
                                        itemRef = (View) iget_object_or_null(activity, "a",
                                                load("com/tencent/mobileqq/widget/FormSimpleItem"));
                                        if (itemRef == null && (itemClass = load(
                                                "com/tencent/mobileqq/widget/FormCommonSingleLineItem"))
                                                != null) {
                                            itemRef = (View) iget_object_or_null(activity, "a", itemClass);
                                        }
                                        if (itemRef == null) {
                                            Class<?> clz = load(
                                                    "com/tencent/mobileqq/widget/FormCommonSingleLineItem");
                                            if (clz == null) {
                                                clz = load("com/tencent/mobileqq/widget/FormSimpleItem");
                                            }
                                            itemRef = (View) ReflexUtil.getFirstNSFByType(activity, clz);
                                        }
                                        View item;
                                        if (itemRef == null) {
                                            // we are in triassic period?
                                            item = (View) new_instance(
                                                    load("com/tencent/mobileqq/widget/FormSimpleItem"),
                                                    activity, Context.class);
                                        } else {
                                            // modern age
                                            item = (View) new_instance(itemRef.getClass(), activity,
                                                    Context.class);
                                        }
                                        XposedBridge.log("[mcHookTool] -> 正在增加列表按钮");
                                        item.setId(R.id.setting2Activity_mcHookTool);
                                        invoke_virtual(item, "setLeftText", "mcHook",
                                                CharSequence.class);
                                        invoke_virtual(item, "setBgType", 2, int.class);
                                        invoke_virtual(item, "setRightText", "设置",
                                                CharSequence.class);
                                        item.setOnClickListener(v -> {
                                            activity.startActivity(new Intent(activity, MainActivity.class));
                                            XposedBridge.log("按下了按钮");
                                        });
                                        if (itemRef != null) {
                                            //modern age
                                            ViewGroup list = (ViewGroup) itemRef.getParent();
                                            ViewGroup.LayoutParams reflp;
                                            if (list.getChildCount() == 1) {
                                                //junk!
                                                list = (ViewGroup) list.getParent();
                                                reflp = ((View) itemRef.getParent()).getLayoutParams();
                                            } else {
                                                reflp = itemRef.getLayoutParams();
                                            }
                                            ViewGroup.LayoutParams lp = null;
                                            if (reflp != null) {
                                                lp = new ViewGroup.LayoutParams(
                                                        MATCH_PARENT, /*reflp.height*/WRAP_CONTENT);
                                            }
                                            int index = 0;
                                            int account_switch = list.getContext().getResources()
                                                    .getIdentifier("account_switch", "id",
                                                            list.getContext().getPackageName());
                                            try {
                                                if (account_switch > 0) {
                                                    View accountItem = (View) list
                                                            .findViewById(account_switch).getParent();
                                                    for (int i = 0; i < list.getChildCount(); i++) {
                                                        if (list.getChildAt(i) == accountItem) {
                                                            index = i + 1;
                                                            break;
                                                        }
                                                    }
                                                }
                                                if (index > list.getChildCount()) {
                                                    index = 0;
                                                }
                                            } catch (NullPointerException ignored) {
                                            }
                                            list.addView(item, index, lp);
                                        } else {
                                            // triassic period, we have to find the ViewGroup ourselves
                                            int qqsetting2_msg_notify = activity.getResources()
                                                    .getIdentifier("qqsetting2_msg_notify", "id",
                                                            activity.getPackageName());
                                            if (qqsetting2_msg_notify == 0) {
                                                throw new UnsupportedOperationException(
                                                        "R.id.qqsetting2_msg_notify not found in triassic period");
                                            } else {
                                                ViewGroup vg = (ViewGroup) activity
                                                        .findViewById(qqsetting2_msg_notify).getParent()
                                                        .getParent();
                                                vg.addView(item, 0, new ViewGroup.LayoutParams(
                                                        MATCH_PARENT, /*reflp.height*/WRAP_CONTENT));
                                            }
                                        }
                                        XposedBridge.log("[mcHookTool] -> 增加完毕");
                                    } catch (Throwable e) {
                                        log(e);
                                        throw e;
                                    }
                                }
                            });
            return true;
        } catch (Throwable e) {
            log(e);
            return false;
        }
    }
}
