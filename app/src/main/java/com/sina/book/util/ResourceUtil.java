package com.sina.book.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.sina.book.SinaBookApplication;

/**
 * 资源文件工具类.
 *
 * @author MarkMjw
 */
public class ResourceUtil {
    private static final String TAG = "ResourceUtil";

    private static Context sContext = SinaBookApplication.gContext;

    /**
     * Gets the color.
     *
     * @param resId the res id
     * @return the color
     */
    public static int getColor(int resId) {
        return sContext.getResources().getColor(resId);
    }

    /**
     * Gets the dimens.
     *
     * @param resId the res id
     * @return the dimens
     */
    public static float getDimens(int resId) {
        return sContext.getResources().getDimension(resId);
    }

    /**
     * Gets the string.
     *
     * @param resId the res id
     * @return the string
     */
    public static String getString(int resId) {
        return sContext.getResources().getString(resId);
    }

    /**
     * Gets the drawable.
     *
     * @param resId the res id
     * @return the drawable
     */
    public static Drawable getDrawable(int resId) {
        return sContext.getResources().getDrawable(resId);
    }

    /**
     * Gets the arrays.
     *
     * @param resId the res id
     * @return the arrays
     */
    public static String[] getStringArrays(int resId) {
        return sContext.getResources().getStringArray(resId);
    }

    /**
     * Gets the arrays.
     *
     * @param resId the res id
     * @return the arrays
     */
    public static int[] getIntArrays(int resId) {
        return sContext.getResources().getIntArray(resId);
    }

    /**
     * Gets the color state list.
     *
     * @param resId the res id
     * @return the color state list
     */
    public static ColorStateList getColorStateList(int resId) {
        return sContext.getResources().getColorStateList(resId);
    }

    /**
     * 通过资源字符串获取对应的资源ID
     *
     * @param resAllName
     * @return 资源ID，or -1
     */
    public static int getIdByName(String resAllName) {
        int resId = -1;
        try {
            Resources res = sContext.getResources();

            if (!TextUtils.isEmpty(resAllName)) {
                int i = resAllName.lastIndexOf(":") + 1;
                int j = resAllName.lastIndexOf("/");

                String type = resAllName.substring(i, j);
                String resName = resAllName.substring(j + 1);
                String packName = sContext.getPackageName();

                resId = res.getIdentifier(resName, type, packName);
            }

        } catch (Exception e) {
            LogUtil.e(TAG, "Don't find the resources id by the string : " + resAllName);
        }

        return resId;
    }

    /**
     * 通过资源ID获取对应的资源字符串(包含包名)
     *
     * @param resId
     * @return 资源名称
     */
    public static String getAllNameById(int resId) {
        String resAllName = "";
        try {
            Resources res = sContext.getResources();
            resAllName = res.getResourceName(resId);
        } catch (Exception e) {
            LogUtil.e(TAG, "Don't find the resources name by the id : " + resId);
        }

        return resAllName;
    }

    /**
     * 通过资源ID获取对应的资源字符串
     *
     * @param resId
     * @return 资源名称
     */
    public static String getNameById(int resId) {
        String resName = "";
        try {
            Resources res = sContext.getResources();
            String resAllName = res.getResourceName(resId);

            if (!TextUtils.isEmpty(resAllName)) {
                int i = resAllName.lastIndexOf("/");

                resName = resAllName.substring(i + 1);
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Don't find the resources name by the id : " + resId);
        }

        return resName;
    }
}
