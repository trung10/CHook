package ai.juyou.hookhelper;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Timer;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class HookHelper {
    private static final String TAG = "CameraHook";

    public static void waitCall(int delay, View view, WaitCallback callback)
    {
        final Timer timer = new Timer();
        timer.schedule(new java.util.TimerTask(){
            public void run() {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.callback(view);
                    }
                });
                timer.cancel();
            }
        }, delay);
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

    public static void printSuperClass(Class cls)
    {
        Log.i(TAG, "getSuperClass: " + cls);
        Class superCls = cls.getSuperclass();
        if(superCls!=null)
            printSuperClass(superCls);
    }

    public static void printDeclaredMethods(Class cls)
    {
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            Log.d(TAG,"Method name: " + method.getName());
            Parameter[] parameters = method.getParameters();
            for (Parameter parameter : parameters) {
                Log.d(TAG,"Parameter name: " + parameter.getName()
                        + ", type: " + parameter.getType().getSimpleName());
            }
        }
    }

    public static void printStackTrace()
    {
        try {
            throw new Exception("printStackTrace");
        }
        catch (Exception e){
            Log.e(TAG, "printStackTrace: ", e);
        }
    }

    public static void hookAllMethods(Class cls, XC_MethodHook callback)
    {
        Method[] methods = cls.getDeclaredMethods();
        for (Method method : methods) {
            XposedBridge.hookMethod(method, callback);
        }
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
