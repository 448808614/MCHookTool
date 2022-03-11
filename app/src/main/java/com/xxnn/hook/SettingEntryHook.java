package com.xxnn.hook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.xxnn.mchooktool.MainActivity;
import com.xxnn.mchooktool.R;
import com.xxnn.ui.CustomDialog;
import com.xxnn.ui.ViewBuilder;
import com.xxnn.utils.ReflexUtil;
import com.xxnn.utils.Utils;
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
                                            showChatWordsCountDialog(activity);
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

    public void showChatWordsCountDialog(Context activity) {
        XposedBridge.log("[mcHookTool] -> 开始" + (activity == null ? "t" : "f"));
        CustomDialog dialog = CustomDialog.createFailsafe(activity);
        Context ctx = dialog.getContext();
        EditText editText = new EditText(ctx);
        editText.setTextSize(16f);
        int _5 = Utils.dip2px(activity, 5f);
        editText.setPadding(_5, _5, _5, _5 * 2);
        editText.setText("http://192.168.8.58/hook");
        CheckBox checkBox = new CheckBox(ctx);
        checkBox.setText("开启数据发送");
        checkBox.setChecked(false);
        LinearLayout linearLayout = new LinearLayout(ctx);
        linearLayout.setOrientation(LinearLayout.VERTICAL);linearLayout.addView(
                ViewBuilder.subtitle(activity, "本程序仅用于学习交流使用"),
                ViewBuilder.newLinearLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        _5,
                        0,
                        _5,
                        0
                )
        );
        linearLayout.addView(
                ViewBuilder.subtitle(activity, "hook发送和接受数据"),
                ViewBuilder.newLinearLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        _5,
                        0,
                        _5,
                        0
                )
        );
        linearLayout.addView(
                checkBox,
                ViewBuilder.newLinearLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        _5 * 2
                )
        );
        linearLayout.addView(
                editText,
                ViewBuilder.newLinearLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        _5 * 2
                )
        );
        AlertDialog alertDialog = (AlertDialog) dialog.setTitle("输入hook服务端地址")
                .setView(linearLayout)
                .setCancelable(true)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNeutralButton("测试", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity, "点击了", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
