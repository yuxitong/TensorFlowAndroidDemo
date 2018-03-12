package com.example.android.tflitecamerademo.utils;/**
 * Created by 30884 on 2017/10/17.
 */

import android.app.ActivityManager;
import android.content.Context;

import java.util.List;

/**
 * --------------------------
 * 作者：鱼欲渔于鱼
 * 时间：2017/10/17
 * 备注：服务工具类
 * --------------------------
 */
public class ServiceUtils {

    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName
     *            是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public static boolean isServiceWork(Context mContext, String serviceName) {
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        return isWork;
    }

}
