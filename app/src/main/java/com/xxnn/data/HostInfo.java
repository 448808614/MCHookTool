package com.xxnn.data;

import android.app.Application;

public class HostInfo {
    private static HostInfo hostInfo;
    private Application context;
    private String packageName;

    public static HostInfo getInstance() {
        if (hostInfo == null) {
            hostInfo = new HostInfo();
        }
        return hostInfo;
    }

    public void init(Application context) {
        this.context = context;
        this.packageName = context.getPackageName();
    }

    public Application getContext() {
        return context;
    }

    public String getPackageName() {
        return packageName;
    }
}
