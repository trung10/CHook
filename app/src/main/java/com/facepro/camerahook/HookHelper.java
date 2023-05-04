package com.facepro.camerahook;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Method;
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


    public static void waitFindChildView(View root, String txt, WaitCallback callback)
    {
        waitFindChildView(root,txt,false,callback);
    }
    public static void waitFindChildView(View root, String txt,boolean isContains, WaitCallback callback)
    {
        final Timer timer = new Timer();
        timer.schedule(new java.util.TimerTask(){
            public void run() {
                View view = findChildView(root,txt,isContains);
                if(view!=null){
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.callback(view);
                        }
                    });
                    timer.cancel();
                }
                else{
                    waitFindChildView(root,txt,isContains,callback);
                }
            }
        }, 1000);
    }
    public static View findChildView(View root, String text)
    {
        return findChildView(root,text,false);
    }
    public static View findChildView(View root, String text,boolean isContains)
    {
        if(hasMethod(root.getClass(),"getText",null)){
            try {
                Method method = root.getClass().getMethod("getText");
                Object obj = method.invoke(root);
                if(obj!=null){
                    String str = obj.toString();
                    if(isContains){
                        if(str.contains(text)){
                            return root;
                        }
                    }
                    else{
                        if(str.equals(text))
                            return root;
                    }
                }
            } catch (Exception e) {

            }
        }
        else{
            if(root instanceof ViewGroup){
                ViewGroup vg = (ViewGroup) root;
                for(int i = 0; i<vg.getChildCount(); i++){
                    View view = vg.getChildAt(i);
                    View result = findChildView(view, text, isContains);
                    if(result!=null)
                        return result;
                }
            }
        }
        return null;
    }

    public static boolean hasMethod(Class clzz, String methodName, Class[] argsType)
    {
        try {
            Method method = clzz.getMethod(methodName, argsType);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    public static void getSuperClass(Class cls)
    {
        Log.i(TAG, "getSuperClass: " + cls);
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
