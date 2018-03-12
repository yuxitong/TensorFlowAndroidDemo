package com.tfcamerademo.utils;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * 尺寸测量工具类
 * @author yxt 2016/8/26
 * 在onCreate()即可强行获取View的尺寸 forceGetViewSize
 * ListView中提前测量View尺寸(注释萌萌哒) measureView
 */
public class SizeUtils {

    private SizeUtils() {
        throw new UnsupportedOperationException("UnsupportedOperationException");
    }


    /**
     * 在onCreate()即可强行获取View的尺寸
     * <p>需回调onGetSizeListener接口，在onGetSize中获取view宽高</p>
     * <p>用法示例如下所示</p>
     * <pre>{@code
     * SizeUtils.forceGetViewSize(view);
     * SizeUtils.setListener(new SizeUtils.onGetSizeListener() {
     *     public void onGetSize(View view) {
     *         Log.d("tag", view.getWidth() + " " + view.getHeight());
     *     }
     * });}
     * </pre>
     *
     * @param view 视图
     */
    public static void forceGetViewSize(final View view) {
        view.post(new Runnable() {
            @Override
            public void run() {
                if (mListener != null) {
                    mListener.onGetSize(view);
                }
            }
        });
    }

    /**
     * 获取到View尺寸的监听
     */
    public interface onGetSizeListener {
        void onGetSize(View view);
    }

    public static void setListener(onGetSizeListener listener) {
        mListener = listener;
    }

    private static onGetSizeListener mListener;

    /**
     * ListView中提前测量View尺寸，如headerView
     * <p>用的时候去掉注释拷贝到ListView中即可</p>
     * <p>参照以下注释代码</p>
     *
     * @param view 视图
     */
    public static void measureViewInLV(View view) {
        Log.i("tips", "U should copy the following code.");
        /*
        ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int width = ViewGroup.getChildMeasureSpec(0, 0, p.width);
        int height;
        int tempHeight = p.height;
        if (tempHeight > 0) {
            height = MeasureSpec.makeMeasureSpec(tempHeight,
                    MeasureSpec.EXACTLY);
        } else {
            height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(width, height);
        */
    }


    /**
     * 多层滑动嵌套无法显示子滑动控件高度的测量解决方案
     * @param listView 内部滑动listView
     */
    public static void ListViewHeightBasedOnChildren(ListView listView) {
            // 获取ListView对应的Adapter
            ListAdapter listAdapter = listView.getAdapter();
            if (listAdapter == null) {
                    return;
                }

            int totalHeight = 0;
            for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
                    // listAdapter.getCount()返回数据项的数目
                    View listItem = listAdapter.getView(i, null, listView);
                    // 计算子项View 的宽高
                    listItem.measure(0, 0);
                    // 统计所有子项的总高度
                    totalHeight += listItem.getMeasuredHeight();
                }

            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
            // listView.getDividerHeight()获取子项间分隔符占用的高度
            // params.height最后得到整个ListView完整显示需要的高度
            listView.setLayoutParams(params);
        }

}
