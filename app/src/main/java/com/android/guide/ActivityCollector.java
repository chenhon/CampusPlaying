package com.android.guide;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/10 0010.
 */

public class ActivityCollector {
    public static List<Activity> activitys = new ArrayList<>();

    public static void addActivity(Activity activity) {
        activitys.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activitys.remove(activity);
    }

    //销毁所有的活动
    public static void finishAll() {
        for (Activity activity:activitys) {
            if(!activity.isFinishing()) {
                activity.finish();
            }
        }
    }
}
