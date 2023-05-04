package com.facepro.camerahook;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import java.util.Timer;

public class HookHelper {
    private static final String TAG = "CameraHook";

    public interface WaitCallback
    {
        void callback(Object obj);
    }

    public static void waitGetChildView(ViewGroup root, int position, WaitCallback callback)
    {
        final Timer timer = new Timer();
        timer.schedule(new java.util.TimerTask(){
            public void run() {
                if(root.getChildCount()>position){
                    View view = root.getChildAt(position);
                    root.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.callback(view);
                        }
                    });
                    timer.cancel();
                }
                else{
                    waitGetChildView(root,position,callback);
                }
            }
        }, 1000);
    }


    public static void getSuperClass(Class cls)
    {
        Log.d(TAG, "getSuperClass: " + cls);
        Class superCls = cls.getSuperclass();
        if(superCls!=null)
            getSuperClass(superCls);
    }

    public static ViewTree getViewTree(Activity activity)
    {
        ViewGroup rootView = (ViewGroup) activity.getWindow().getDecorView();
        return new ViewTree(rootView);
    }

    public static ViewTree getViewTree(ViewGroup rootView)
    {
        return new ViewTree(rootView);
    }
}
