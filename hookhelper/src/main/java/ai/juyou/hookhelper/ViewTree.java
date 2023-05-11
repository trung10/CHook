package ai.juyou.hookhelper;


import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ViewTree {
    private static final String TAG = "CameraHook";
    private final ViewGroup mRootView;
    private final StringBuffer mStringBuffer;
    private final List<View> mViewList;
    public ViewTree(ViewGroup rootView) {
        mRootView = rootView;
        mStringBuffer = new StringBuffer();
        mViewList = new ArrayList<>();
        traversalView(rootView,mViewList, mStringBuffer, "|");
    }

    private static void checkView(View view,List<View> mViewList, StringBuffer sb, String prefix)
    {
        mViewList.add(view);
        String strId = prefix + view.getClass() + ":"+view.hashCode() + "@"+(mViewList.size()-1);
        sb.append(strId).append("\n");
    }

    private static void traversalView(ViewGroup rootView,List<View> mViewList,StringBuffer sb, String prefix) {
        checkView(rootView,mViewList,sb,prefix);
        prefix = prefix+"-";
        for(int i = 0; i<rootView.getChildCount(); i++) {
            View childVg = rootView.getChildAt(i);
            if(childVg instanceof ViewGroup)
                traversalView((ViewGroup) childVg,mViewList,sb, prefix);
            else{
                checkView(childVg,mViewList,sb,prefix);
            }
        }
    }

    public int getViewCount() {
        return mViewList.size();
    }

    public View getView(int index) {
        if(index<0 || index>=mViewList.size())
            return null;
        return mViewList.get(index);
    }

    public ViewGroup getRootView() {
        return mRootView;
    }

    @NonNull
    @Override
    public String toString() {
        return mStringBuffer.toString();
    }
}
