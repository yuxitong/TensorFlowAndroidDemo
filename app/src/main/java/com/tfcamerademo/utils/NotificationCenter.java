package com.example.android.tflitecamerademo.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yxt on 2016/10/28.
 */
public class NotificationCenter {
    public interface NotificationCenterObserver{
        void onReceive(String eventName, Object userInfo);
    }
    public static NotificationCenter defaultCenter;
    public static NotificationCenter defaultCenter(){
        if (defaultCenter== null){
            defaultCenter = new NotificationCenter();
        }
        return defaultCenter;
    }
    public Map<String,List<NotificationCenterObserver>> observerMap = new HashMap<String, List<NotificationCenterObserver>>();
    public void addObserver(NotificationCenterObserver who,String eventName){
        if (observerMap.containsKey(eventName)){
            List<NotificationCenterObserver> list = observerMap.get(eventName);
            list.add(who);
        }
        else{
            List<NotificationCenterObserver> list = new ArrayList<NotificationCenterObserver>();
            list.add(who);
            observerMap.put(eventName, list);
        }
    }
    public void postNotification(String eventName,Object userInfo){
        if (observerMap.containsKey(eventName)){
            List<NotificationCenterObserver> list = observerMap.get(eventName);
            for (int i=0;i<list.size();i++){
                NotificationCenterObserver notificationCenterObserver = list.get(i);
                notificationCenterObserver.onReceive(eventName, userInfo);
            }
        }
    }
    public void removeObserver(NotificationCenterObserver who,String eventName) {
        // TODO Auto-generated method stub
        List<NotificationCenterObserver> list = observerMap.get(eventName);
        try {
            list.remove(who);
        }catch (Exception e){
        }
    }
}
