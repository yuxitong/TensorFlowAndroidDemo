package com.tfcamerademo.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;


/**
 * 转换工具类
 *
 * @author yxt 2016/8/26
 * byteArr与hexString互转 bytes2HexString、hexString2Bytes
 * charArr与byteArr互转 chars2Bytes、bytes2Chars
 * inputStream与byteArr互转 inputStream2Bytes、bytes2InputStream
 * inputStream与string按编码互转 inputStream2String、string2InputStream
 * bitmap与byteArr互转 bitmap2Bytes、bytes2Bitmap
 * drawable与bitmap互转 drawable2Bitmap、bitmap2Drawable
 * drawable与byteArr互转 drawable2Bytes、bytes2Drawable
 * dp与px互转 dip2px、px2dip
 * sp与px互转 sp2px、px2sp
 * 各种单位转换 applyDimension
 */
public class ConvertUtils {


    static final char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    /**
     * 每1个byte转为2个hex字符
     * <p>例如：</p>
     * bytes2HexString(new byte[] { 0, (byte) 0xa8 }) returns 00A8
     *
     * @param src byte数组
     * @return 16进制大写字符串
     */
    public static String bytes2HexString(byte[] src) {
        char[] res = new char[src.length << 1];
        for (int i = 0, j = 0; i < src.length; i++) {
            res[j++] = hexDigits[src[i] >>> 4 & 0x0f];
            res[j++] = hexDigits[src[i] & 0x0f];

        }
        return new String(res);

    }


    /**
     * 每2个hex字符转为1个byte
     * <p>例如：</p>
     * hexString2Bytes("00A8") returns { 0, (byte) 0xA8 }
     *
     * @param hexString 十六进制字符串
     * @return byte数组
     */
    public static byte[] hexString2Bytes(String hexString) {
        int len = hexString.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("长度不是偶数");

        }
        char[] hexBytes = hexString.toUpperCase().toCharArray();
        byte[] res = new byte[len >>> 1];
        for (int i = 0; i < len; i += 2) {
            res[i >> 1] = (byte) (hex2Dec(hexBytes[i]) << 4 | hex2Dec(hexBytes[i + 1]));

        }
        return res;

    }

    /**
     * 单个hex字符转为10进制
     *
     * @param hexChar hex单个字节
     * @return 0..15
     */
    private static int hex2Dec(char hexChar) {
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';

        } else if (hexChar >= 'A' && hexChar <= 'F') {
            return hexChar - 'A' + 10;

        } else {
            throw new IllegalArgumentException();

        }

    }

    /**
     * charArr转byteArr
     *
     * @param chars 待转的char数组
     * @return byte数组
     */
    public static byte[] chars2Bytes(char[] chars) {
        int len = chars.length;
        byte[] bytes = new byte[len];
        for (int i = 0; i < len; i++) {
            bytes[i] = (byte) (chars[i]);

        }
        return bytes;

    }

    /**
     * byteArr转charArr
     *
     * @param bytes 待转的byte数组
     * @return char数组
     */
    public static char[] bytes2Chars(byte[] bytes) {
        int len = bytes.length;
        char[] chars = new char[len];
        for (int i = 0; i < len; i++) {
            chars[i] = (char) (bytes[i] & 0xff);

        }
        return chars;

    }

    /**
     * 将输入流转为字节数组
     *
     * @param is 输入流
     * @return 字节数组
     */
    public static byte[] inputStream2Bytes(InputStream is) {
        if (is == null) return null;
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] b = new byte[ConstUtils.KB];
            int len;
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);

            }
            return os.toByteArray();

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        } finally {
            FileUtils.closeIO(is);

        }

    }


    /**
     * 将字节数组转为输入流
     *
     * @param bytes 字节数组
     * @return 输入流
     */
    public static InputStream bytes2InputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);

    }


    /**
     * 指定编码将输入流转为字符串
     *
     * @param is          输入流
     * @param charsetName 编码格式
     * @return 字符串
     */
    public static String inputStream2String(InputStream is, String charsetName) {
        if (is == null) return null;
        BufferedReader reader;
        try {
            StringBuilder sb = new StringBuilder();
            if (StringUtils.isSpace(charsetName)) {
                reader = new BufferedReader(new InputStreamReader(is));

            } else {
                reader = new BufferedReader(new InputStreamReader(is, charsetName));

            }
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\r\n");// windows系统换行为\r\n，Linux为\n

            }
            // 要去除最后的换行符
            return sb.delete(sb.length() - 2, sb.length()).toString();

        } catch (IOException e) {
            e.printStackTrace();
            return null;

        } finally {
            FileUtils.closeIO(is);

        }

    }


    /**
     * 指定编码将字符串转为输入流
     *
     * @param string      字符串
     * @param charsetName 编码格式
     * @return 输入流
     */
    public static InputStream string2InputStream(String string, String charsetName) {
        if (string == null || StringUtils.isSpace(charsetName)) return null;
        try {
            return new ByteArrayInputStream(string.getBytes(charsetName));

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;

        }

    }

    /**
     * sp转px
     *
     * @param context 上下文
     * @param spValue sp值
     * @return px值
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    /**
     * px转sp
     *
     * @param context 上下文
     * @param pxValue px值
     * @return sp值
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }
    /**
     * dip转px
     *
     * @param context 上下文
     * @param dipValue dip值
     * @return px值
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * dip转px
     *
     * @param context 上下文
     * @param pxValue px值
     * @return dip值
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * 各种单位转换
     * <p>该方法存在于TypedValue</p>
     *
     * @param unit    单位
     * @param value   值
     * @param metrics DisplayMetrics
     * @return 转换结果
     */
    public static float applyDimension(int unit, float value, DisplayMetrics metrics) {
        switch (unit) {
            case TypedValue.COMPLEX_UNIT_PX:
                return value;
            case TypedValue.COMPLEX_UNIT_DIP:
                return value * metrics.density;
            case TypedValue.COMPLEX_UNIT_SP:
                return value * metrics.scaledDensity;
            case TypedValue.COMPLEX_UNIT_PT:
                return value * metrics.xdpi * (1.0f / 72);
            case TypedValue.COMPLEX_UNIT_IN:
                return value * metrics.xdpi;
            case TypedValue.COMPLEX_UNIT_MM:
                return value * metrics.xdpi * (1.0f / 25.4f);
        }
        return 0;
    }
}
