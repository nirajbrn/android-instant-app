package com.simpl.android.sdk.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

/*import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;*/
import com.simpl.android.sdk.BuildConfig;
import com.simpl.android.sdk.Simpl;
import com.simpl.android.sdk.SimplParam;
import com.simpl.android.sdk.view.activity.BaseSimplScreen;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.content.Context.ACCOUNT_SERVICE;
import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.WIFI_SERVICE;

/**
 * {@link FingerPrintUtil}  is used to collect parameter
 * to be used in fingerprinting of Simpl User
 * There are 3 kind of responses we are sending to server:
 * 1) if permission is not provided by User -> PERMISSION_DISABLED
 * 2) If permission is given but merchant doesn't want to us to fetch those info -> MERCHANT_DISABLED
 * 3) If Everything fine -> "Normal response"
 * @author nirajbrn
 * @date 09/01/17.
 * @email niraj@getsimpl.com
 */

public class FingerPrintUtil /*implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener*/{
    public static final String TAG = FingerPrintUtil.class.getSimpleName();

    private Context context;
    private TelephonyManager telephonyManager;
    private ArrayList<SimplParam> simplParams;
    //private GoogleApiClient googleApiClient;
    private AtomicBoolean noTimeout = new AtomicBoolean();
    private FingerPrintListener listener;
    private Timer timer;


    /**
     * Permission codes
     */
    public static final String ACCOUNT_PERM_CODE = "account_perm";
    public static final String LOCATION_PERM_CODE = "location_perm";
    public static final String CALL_PERM_CODE = "call_perm";
    public static final String PHONE_PERM_CODE = "phone_perm";
    private static final String MERCHANT_DISABLED = "m_disabled";
    private static final String PERMISSION_DISABLED = "p_disabled";

    /**
     * Permissions Flag
     */
    public static final String SIMPL_Ac = "nchnvjnfjnnvnjnvnfnjvnjjfnjvjnnvf";
    public static final String SIMPL_Ps = "mvjkvkkjnvmmklmvllmklmkvlkmmklmklml";
    public static final String SIMPL_Cl = "mkvkmvkkmkmfkkffifmfkmfmfmfmfmfmmmm";
    public static final String SIMPL_Lnt = "iroiriroorkimfimfifkrkrokrmfkmorkr";

    /**
     * Constructor
     *
     * @param context current activity context
     */
    public FingerPrintUtil(Context context) {
        this.context = context;
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        this.simplParams = new ArrayList<>();
        this.timer = new Timer();
    }

    /**
     * Add defined parameter to {@link SimplParam}
     */
    public void setSimplParams(final FingerPrintListener listener) {
        this.listener = listener;
        simplParams.add(SimplParam.create("SIMPL-isR", String .valueOf(RootDeviceUtil.isDeviceRooted())));
        simplParams.add(SimplParam.create("sdk-version", BuildConfig.VERSION_NAME));
        addCarrierName();
        addSerialNumber();
        addUpTimeSinceBoot();
        addAvailableMemory();
        //addIpAddress();
        addInstalledApp();
        addAndroidId();
        addParentAppVersion();
        addWifiSSID();
        addBatteryLevel();
        addAccounts();
        addDeviceIdSimSerialNo();
        getMacAddr();
        //addCellId();
        addCallDetails();
        buildGoogleApiClient();
        if (!noTimeout.getAndSet(true)) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG, "setSimplParams(): ");
                    noTimeout.set(false);
                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onParamsAvailable(simplParams);
                            }
                        });
                    } else {
                        listener.onParamsAvailable(simplParams);
                    }
                }
            }, 300);
        }
    }

    /**
     *
     */
    private void addCarrierName() {
        String carrierName = telephonyManager.getNetworkOperatorName();
        boolean isRoaming = telephonyManager.isNetworkRoaming();
        simplParams.add(SimplParam.create("SIMPL-CaR", String.valueOf(isRoaming)));
        simplParams.add(SimplParam.create("SIMPL-CaN", String.valueOf(carrierName)));
    }

    private void addSerialNumber() {
        simplParams.add(SimplParam.create("SIMPL-SeN", Build.SERIAL));
    }

    private void addUpTimeSinceBoot() {
        simplParams.add(SimplParam.create("SIMPL-Up", String.valueOf(SystemClock.elapsedRealtime()) + "ns"));
    }

    private void addIpAddress() {
        StringBuilder ipConfig = new StringBuilder();
        try {
            for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces(); networkInterfaces.hasMoreElements();) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                for (Enumeration<InetAddress> enumIpAddress = networkInterface.getInetAddresses(); enumIpAddress.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddress.nextElement();
                    ipConfig.append(inetAddress.getHostAddress());
                    ipConfig.append(" ");
                }
            }
        } catch (SocketException ex) {
            Log.e("LOG_TAG", ex.toString());
        }
        simplParams.add(SimplParam.create("SIMPL-IPA", ipConfig.toString()));
    }

    private void addAvailableMemory() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        simplParams.add(SimplParam.create("SIMPL-Amem", String.valueOf(mi.availMem / 1048576L) + "MB"));
    }

    private void addInstalledApp() {
        StringBuilder appBuilder = new StringBuilder();
        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> appInfoList = pm.getInstalledApplications(0);

        for (ApplicationInfo applicationInfo : appInfoList) {
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                appBuilder.append(applicationInfo.packageName);
                appBuilder.append(",");
            }
        }
        simplParams.add(SimplParam.create("SIMPL-InApp", appBuilder.toString()));
    }

    private void addAndroidId() {
        simplParams.add(SimplParam.create("SIMPL-AndId", Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)));
    }

    private void addParentAppVersion() {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            simplParams.add(SimplParam.create("SIMPL-PAV", pInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void addWifiSSID() {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        simplParams.add(SimplParam.create("SIMPL-WIFI-SSID", wifiInfo.getSSID()));
    }

    private void addBatteryLevel() {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            simplParams.add(SimplParam.create("SIMPL-BAT", String.valueOf(((float) level / (float) scale) * 100.0f)));
        }
    }

    private void addAccounts() {
        StringBuilder accountBuilder = new StringBuilder();
        if (!Simpl.getInstance().getDisabled().contains(ACCOUNT_PERM_CODE)) {
            AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
            for (Account account : accountManager.getAccounts()) {
                accountBuilder.append(account.name);
                accountBuilder.append(",");
            }
        } else {
            accountBuilder.append(MERCHANT_DISABLED);
        }
        if (accountBuilder.length() == 0)
            accountBuilder.append(PERMISSION_DISABLED);
        simplParams.add(SimplParam.create("SIMPL-AccEm", accountBuilder.toString()));
    }

    private void addDeviceIdSimSerialNo() {
        String deviceId = MERCHANT_DISABLED, serialNo = MERCHANT_DISABLED, phoneNum = MERCHANT_DISABLED;
        if (!Simpl.getInstance().getDisabled().contains(PHONE_PERM_CODE)) {
            try {
                deviceId = telephonyManager.getDeviceId();
                serialNo = telephonyManager.getSimSerialNumber();
                phoneNum = telephonyManager.getLine1Number();
            } catch (Exception e) {
                deviceId = PERMISSION_DISABLED;
                serialNo = PERMISSION_DISABLED;
                phoneNum = PERMISSION_DISABLED;
                Log.i(TAG, "addDeviceIdSimSerialNo(): Failed while making request" + e.getMessage());
            }
        }
        simplParams.add(SimplParam.create("SIMPL-DevId", deviceId));
        simplParams.add(SimplParam.create("SIMPL-SSN", serialNo));
        simplParams.add(SimplParam.create("SIMPL-PhN", phoneNum));
        simplParams.add(SimplParam.create("SIMPL-DEVICE-MANUFACTURER", Build.MANUFACTURER));
        simplParams.add(SimplParam.create("SIMPL-DEVICE-MODEL", Build.MODEL));
    }

    private void addCellId() {
        String cellId = MERCHANT_DISABLED;
        if (!Simpl.getInstance().getDisabled().contains(PHONE_PERM_CODE)) {
            try {
                GsmCellLocation cellLocation = (GsmCellLocation) telephonyManager.getCellLocation();
                cellId = String.valueOf(cellLocation.getCid());
            } catch (Exception e) {
                cellId = PERMISSION_DISABLED;
                Log.i(TAG, "addCellId(): Failed while making request" + e.getMessage());
            }
        }
        simplParams.add(SimplParam.create("SIMPL-Cid", cellId));
    }

    private void addCallDetails() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            String callDetails = MERCHANT_DISABLED;
            if (!Simpl.getInstance().getDisabled().contains(CALL_PERM_CODE)) {
                try {
                    Cursor cur = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");
                    if (cur != null) {
                        if (cur.moveToNext()) {
                            String phNumber = cur.getString(cur.getColumnIndex(CallLog.Calls.NUMBER));
                            String callDuration = cur.getString(cur.getColumnIndex(CallLog.Calls.DURATION));
                            callDetails = phNumber + ", " + callDuration + "sec";
                        }
                        cur.close();
                    }
                } catch (Exception e) {
                    callDetails = PERMISSION_DISABLED;
                    Log.i(TAG, "addCellId(): Failed while making request" + e.getMessage());
                }
            }
            simplParams.add(SimplParam.create("SIMPL-Lcd", callDetails));
        }
    }

    private synchronized void buildGoogleApiClient() {
        /*googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();*/
    }

    /*@Override
    public void onConnected(Bundle bundle) {
        String location = MERCHANT_DISABLED;
        if (!Simpl.getInstance().getDisabled().contains(LOCATION_PERM_CODE)) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                boolean isMocked;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    isMocked = lastLocation.isFromMockProvider();
                } else {
                    isMocked = Settings.Secure.getString(context.getContentResolver(),
                            Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
                }
                location = String.valueOf(lastLocation.getLatitude()) +
                        ", " + String.valueOf(lastLocation.getLongitude());
                simplParams.add(SimplParam.create("SIMPL-isMock", String.valueOf(isMocked)));
            } else {
                location = PERMISSION_DISABLED;
                Log.d(TAG, "onConnected(): Not getting location");
            }
            googleApiClient.disconnect();
        }
        simplParams.add(SimplParam.create("SIMPL-Ltln", location));
        if (noTimeout.getAndSet(true)) {
            timer.cancel();
            Log.d(TAG, "onConnected(): ");
            listener.onParamsAvailable(simplParams);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        simplParams.add(SimplParam.create("SIMPL-Ltln", "Location_connection-suspended"));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        simplParams.add(SimplParam.create("SIMPL-Ltln", "Location_connection-failed"));

    }*/

    public interface FingerPrintListener {
        void onParamsAvailable(ArrayList<SimplParam> param);
    }

    private void getMacAddr() {
        String macAddress = "02:00:00:00:00:00";
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes != null) {

                    StringBuilder macAdd = new StringBuilder();
                    for (byte b : macBytes) {
                        macAdd.append(Integer.toHexString(b & 0xFF));
                        macAdd.append(":");
                    }
                    if (macAdd.length() > 0) {
                        macAdd.deleteCharAt(macAdd.length() - 1);
                    }
                    macAddress = macAdd.toString();
                }
            }
        } catch (Exception ex) {
        }
        simplParams.add(SimplParam.create("SIMPL-Mac", macAddress));
    }
}
