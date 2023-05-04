package com.facepro.camerahook;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.GridView;

import java.util.Timer;

public class HookHelper {
    private static final String TAG = "CameraHook";


    private static class MyInteger
    {
        public int value;
        public MyInteger(int value)
        {
            this.value = value;
        }
        public void plus()
        {
            value++;
        }

        public int getValue()
        {
            return value;
        }
    }

    private interface WaitCallback
    {
        void callback(Object obj);
    }

    private static Timer timer;
    private static void waitGetListItem(Adapter adapter,int position,WaitCallback callback)
    {
        timer = new Timer();
        timer.schedule(new java.util.TimerTask(){
            public void run() {
                if(adapter.getCount()>position)
                {
                    Object item = adapter.getItem(position);
                    callback.callback(item);
                    timer.cancel();
                    timer = null;
                }
                else{
                    waitGetListItem(adapter,position,callback);
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

    private static void checkView(View view, StringBuffer sb, String prefix, MyInteger index)
    {
        index.plus();
        int id = index.getValue();
        if(id==46) {
            GridView gridView = (GridView) view;
            waitGetListItem(gridView.getAdapter(), 0, new WaitCallback() {
                @Override
                public void callback(Object obj) {
                    Log.d(TAG, "waitGetListItem: " + obj.getClass());
                    getSuperClass(obj.getClass());
                }
            });
        }

        String strId = prefix + view.getClass() + "@"+id;
        sb.append(strId+"\n");
    }

    private static void traversalView(ViewGroup rootView, StringBuffer sb, String prefix, MyInteger index) {
        checkView(rootView,sb,prefix,index);
        prefix = prefix+"-";
        for(int i = 0; i<rootView.getChildCount(); i++) {
            View childVg = rootView.getChildAt(i);
            if(childVg instanceof ViewGroup)
                traversalView((ViewGroup) childVg,sb, prefix,index);
            else{
                checkView(childVg,sb,prefix,index);
            }
        }
    }

    public static void checkView(Activity activity, boolean isPrint)
    {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        StringBuffer sb = new StringBuffer();
        traversalView(decorView,sb,"|",new MyInteger(0));
        if(isPrint)
        {
            Log.d(TAG, sb.toString());
        }
    }
}
