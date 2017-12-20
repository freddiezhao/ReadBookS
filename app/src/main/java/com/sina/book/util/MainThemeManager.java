package com.sina.book.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
import com.sina.book.R;

import java.util.Collection;
import java.util.HashMap;

/**
 * 主页主题管理器
 * 
 * @author Tsimle
 * 
 */
public class MainThemeManager {
    public static String ACTION_MAIN_THEME_CHANGED = "com.sina.book.action.MAINTHEMECHANGED";

    private static final int THEME_TYPE_DEF = 0;

    private static MainThemeManager mInstance = new MainThemeManager();
    private HashMap<Integer, MainTheme> themes = new HashMap<Integer, MainTheme>();
    private MainTheme curTheme;

    private MainThemeManager() {
        // 默认主题
        MainTheme theme0 = new MainTheme(THEME_TYPE_DEF, "1", "");
        MainTheme theme1 = new MainTheme(1, "2", "_1");
        MainTheme theme2 = new MainTheme(2, "3", "_2");
        MainTheme theme3 = new MainTheme(3, "4", "_3");
        MainTheme theme4 = new MainTheme(4, "5", "_4");
        
        themes.put(theme0.type, theme0);
        themes.put(theme1.type, theme1);
        themes.put(theme2.type, theme2);
        themes.put(theme3.type, theme3);
        themes.put(theme4.type, theme4);

        int typeSaved = StorageUtil.getInt(StorageUtil.KEY_MAIN_THEME_TYPE, THEME_TYPE_DEF);
        if (typeSaved >= themes.size() && typeSaved < 0) {
            typeSaved = 0;
        } 
        curTheme = themes.get(typeSaved);
    }

    public static MainThemeManager getInstance() {
        return mInstance;
    }

    public Collection<MainTheme> getThemes() {
        return themes.values();
    }

    public MainTheme getCurTheme() {
        return curTheme;
    }

    public void setCurTheme(Context context, int type) {
        setCurTheme(context, themes.get(type));
    }

    public void setCurTheme(Context context, MainTheme changeTheme) {
        if (changeTheme.equals(curTheme)) {
            return;
        }
        this.curTheme = changeTheme;
        StorageUtil.saveInt(StorageUtil.KEY_MAIN_THEME_TYPE, curTheme.type);

        // 发送主题变化通知
        Intent intent = new Intent();
        intent.setAction(ACTION_MAIN_THEME_CHANGED);
        context.sendBroadcast(intent);

        Toast.makeText(context,
                context.getString(R.string.main_theme_change),
                Toast.LENGTH_SHORT).show();
        ;
    }

    /**
     * 得到图片资源，会根据当前阅读模式做资源替换
     * 
     * @param context
     * @param resId
     * @return
     */
    public Drawable getDrawableFromIdentifier(Context context, int resId) {
    	
        if (curTheme.type != THEME_TYPE_DEF) {
            Drawable drawable = null;
            Resources res = context.getResources();
            String resName = res.getResourceName(resId);
            int j = resName.lastIndexOf("/") + 1;
            String ngName = resName.substring(j) + curTheme.suffix;
            int ngResId = res.getIdentifier(ngName, "drawable",
                    context.getPackageName());
            try {
                drawable = context.getResources().getDrawable(ngResId);
            } catch (Exception e) {
                drawable = null;
            }
            if (drawable != null) {
                return drawable;
            }
        }
        return context.getResources().getDrawable(resId);
    }

    public static class MainTheme {
        public int type = 0;
        String name;
        String suffix;

        public MainTheme(int type, String name, String suffix) {
            this.type = type;
            this.name = name;
            this.suffix = suffix;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + type;
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof MainTheme) {
                MainTheme theme = (MainTheme) o;
                if (this == theme) {
                    return true;
                }

                if (this.type == theme.type) {
                    return true;
                }
            }
            return false;
        }
    }
}
