package com.example.android.tflitecamerademo.utils;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class EasyUI {
    public static void setButtonOnClickListener(View view,int id,OnClickListener onClickListener){
        View testView = view.findViewById(id);
        if(testView == null){
            Log.d("EasyUI", "testView is null");
            return ;
        }
        if(!Button.class.isAssignableFrom(testView.getClass())&&!ImageButton.class.isAssignableFrom(testView.getClass())){
            Log.d("EasyUI", "testView is not a button");
            return ;
        }
        if(Button.class.isAssignableFrom(testView.getClass())){
            Button button = (Button) testView;
            button.setOnClickListener(onClickListener);
        }else if(ImageButton.class.isAssignableFrom(testView.getClass())){
            ImageButton button = (ImageButton) testView;
            button.setOnClickListener(onClickListener);
        }

    }
    public static Method getMethod(Class<? extends Object> clazz, String methodName) throws Exception {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    return method;
                } else {
                    method = getMethod(clazz.getSuperclass(), methodName);
                }
            }
        }
        return method;
    }

    public static void setButtonClickMethod(View button, final Object instance,String methodName){

        Class<? extends Object> clazz = instance.getClass();
        Method declaredMethod = null;
        boolean hasParams = false;
        try {
            declaredMethod = clazz.getDeclaredMethod(methodName);

        } catch (NoSuchMethodException e) {

        }
        try {
            declaredMethod = clazz.getDeclaredMethod(methodName, View.class);
            hasParams = true;
        } catch (NoSuchMethodException e) {

        }
        if(declaredMethod == null){
            return ;
        }
        final Method finalDeclaredMethod = declaredMethod;
        final boolean finalHasParams = hasParams;
        button .setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if (finalHasParams){
                        finalDeclaredMethod.invoke(instance,view);
                    }else {
                        finalDeclaredMethod.invoke(instance);
                    }
                } catch (IllegalAccessException e) {
                    int i = 0 ;
                    i++;

                } catch (InvocationTargetException e) {
                    int i = 0 ;
                    i++;
                }
            }
        });
    }

    public static TextView findTextViewById(ViewGroup root,int id){
        return (TextView) root.findViewById(id);
    }
    public static String getTextViewText(View view){
        return getTextViewText(view,null);
    }
    public static String getTextViewText(View view, String defaultText){
        try {
            TextView textView = (TextView)view;
            CharSequence text = textView.getText();
            if (text == null){
                return defaultText;
            }
            return text.toString();
        }catch (Exception e){
            return defaultText;
        }

    }
//    public static void setTextViewText(View view,JsonElement data,String path,String defaultText){
//        try {
//            TextView textView = (TextView) view;
//            try {
//                textView.setText(GsonValidate.getStringByKeyPath(data,path,defaultText));
//            }catch (Exception e1){
//                textView.setText(defaultText);
//            }
//        }catch (Exception e2){
//
//        }
//    }

//    public static void setEditTextText(View view,JsonElement data,String path,String defaultText){
//        try {
//            EditText editText = (EditText) view;
//            try {
//                editText.setText(GsonValidate.getStringByKeyPath(data,path,defaultText));
//            }catch (Exception e1){
//                editText.setText(defaultText);
//            }
//        }catch (Exception e2){
//
//        }
//    }

//    public static void setTextViewTextById(ViewGroup root,int id,JsonElement data,String path,String defaultText){
//
//        EasyUI.setTextViewText(root.findViewById(id), data, path, defaultText);
//    }
//    public static void setImageViewLoadUri(View image,JsonElement data,String path,String defaultUri) {
//        try {
//            ImageView imageView = (ImageView) image;
//            try {
//                String string = GsonValidate.getStringByKeyPath(data, path, defaultUri);
//                String strUri = string.trim();
//                ImageLoader.getInstance().displayImage(strUri,imageView);
//            }catch (Exception e){
//                ImageLoader.getInstance().displayImage(defaultUri,imageView);
//                Log.w("easy ui",e);
//            }
//        }catch (Exception e2){
//            Log.w("easy ui",e2);
//        }
//    }
//    public static void imageViewLoadUri(ViewGroup root,int id,JsonElement data,String path,String defaultUri){
//        EasyUI.setImageViewLoadUri(root.findViewById(id),data,path,defaultUri);
//    }


    public static ViewGroup.LayoutParams fillParentLayout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT);
    public static LinearLayout.LayoutParams scaleHeightFillWidthLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT , 0 , 1);
    public static LinearLayout.LayoutParams scaleWidthFillHeightLayout = new LinearLayout.LayoutParams(0 , ViewGroup.LayoutParams.FILL_PARENT , 1);

}
