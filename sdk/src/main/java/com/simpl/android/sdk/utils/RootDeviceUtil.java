package com.simpl.android.sdk.utils;

import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author nirajbrn
 * @date 03/01/17.
 * @email niraj@getsimpl.com
 */

public final class RootDeviceUtil {
    public static boolean isDeviceRooted() {
        RootDeviceUtil rootDeviceUtil = new RootDeviceUtil();
        return rootDeviceUtil.isRootByTag() || rootDeviceUtil.isRootBySuFile() || rootDeviceUtil.isRootBySuProcess();
    }

    private boolean isRootByTag() {
        String buildTag = Build.TAGS;
        return buildTag != null && buildTag.contains("test-keys");
    }

    private boolean isRootBySuFile() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists())
                return true;
        }
        return false;
    }

    private boolean isRootBySuProcess() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }


}
