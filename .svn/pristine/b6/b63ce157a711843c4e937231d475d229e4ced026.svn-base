package com.sina.book.ui.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Fragment基类，初次请求时在onSelected方法内完成数据请求
 * 
 * @author Tsmile
 * 
 */
public abstract class BaseFragment extends Fragment {

    /**
     * 是否需要默认显示该fragment，<br>
     * 当true时在加载完view之后会去调用onSelected方法
     */
    private boolean mIsDefaultFragment = false;

    /**
     * 该Fragment是否已销毁
     */
    private boolean mIsDestroyed;
    private Toast mToast;
    
    public void setDefaultFragment() {
        mIsDefaultFragment = true;
    }

    protected boolean isDestroyed() {
        return mIsDestroyed;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mIsDefaultFragment) {
            mIsDefaultFragment = false;
            onSelected();
        }
        mIsDestroyed = false;
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        mIsDestroyed = true;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        mIsDestroyed = true;
        super.onDestroy();
    }
    
    protected void shortToast(int resId) {
        shortToast(getString(resId));
    }

    protected void shortToast(String content) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getActivity(), content,
                Toast.LENGTH_SHORT);
        mToast.show();
    }
    
    /**
     * 当Fragment被选中时调用
     */
    public abstract void onSelected();
    
    /**
     * 这一对方法在需要释放的情况下同时使用<br>
     * 父Activity在pause时onRelease<br>
     * 在resume时onLoad<br>
     */
    public void onRelease(){};
    public void onLoad(){};
    
    /**
     * 所在的FragmentActivity会派生点击事件
     * @param keyCode
     * @param event
     * @return
     */
    public boolean onKeyDown(int keyCode, KeyEvent event){
        return false;
    };
}
