package com.tfcamerademo.utils;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;


/**
 * 设备工具类
 *
 * @author yxt 2016/8/26
 * 获取设备MAC地址 getMacAddress
 * 获取设备厂商，如Xiaomi getManufacturer
 * 获取设备型号，如MI2SC getModel
 */
public class DeviceUtils {


    private DeviceUtils() {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }


    /**
     * 获取设备MAC地址
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>}</p>
     *
     * @param context 上下文
     * @return MAC地址
     */
    public static String getMacAddress(Context context) {
        WifiManager wifi = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        String macAddress = info.getMacAddress().replace(":", "");
        return macAddress == null ? "" : macAddress;
    }


    /**
     * 获取设备MAC地址
     * <p>
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>}</p>
     *
     * @return MAC地址
     */
    public static String getMacAddress() {
        String macAddress = null;
        LineNumberReader reader = null;
        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            reader = new LineNumberReader(ir);
            macAddress = reader.readLine().replace(":", "");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (reader != null) reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return macAddress == null ? "" : macAddress;
    }


    /**
     * 获取设备厂商，如Xiaomi
     *
     * @return 设备厂商
     */
    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }


    /**
     * 获取设备型号，如MI2SC
     *
     * @return 设备型号
     */
    public static String getModel() {
        String model = Build.MODEL;
        if (model != null) {
            model = model.trim().replaceAll("\\s*", "");
        } else {
            model = "";
        }
        return model;
    }

    private static String _getSystemProperties(String field) {
        String result = null;
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get",
                    new Class<?>[] { String.class });
            result = (String) getMethod.invoke(classType,
                    new Object[] { field });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    //获取软件版本号
    public static String getSoftwareVersion() {
        String getSoftwareVersion= _getSystemProperties("banben.product.swversion");
        if(getSoftwareVersion !=null){
            return getSoftwareVersion;
        }else{
            return "";
        }
    }


}
