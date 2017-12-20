package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;

/**
 * 分类适配器.
 */
public abstract class CategoryAdapter extends BaseAdapter {

    /**
     * 分类.
     */
    public class Category {

        /** The m title. */
        private String mTitle;

        /** The m adapter. */
        private Adapter mAdapter;

        /**
         * Instantiates a new category.
         * 
         * @param title
         *            the title
         * @param adapter
         *            the adapter
         */
        public Category(String title, Adapter adapter) {
            mTitle = title;
            mAdapter = adapter;
        }

        /**
         * Sets the tile.
         * 
         * @param title
         *            the new tile
         */
        public void setTile(String title) {
            mTitle = title;
        }

        /**
         * Gets the title.
         * 
         * @return the title
         */
        public String getTitle() {
            return mTitle;
        }

        /**
         * Sets the adapter.
         * 
         * @param adapter
         *            the new adapter
         */
        public void setAdapter(Adapter adapter) {
            mAdapter = adapter;
        }

        /**
         * Gets the adapter.
         * 
         * @return the adapter
         */
        public Adapter getAdapter() {
            return mAdapter;
        }
    }

    /** 分类集合. */
    private List<Category> categories = new ArrayList<Category>();

    /**
     * 更新分类标题.
     *
     * @param index the index
     * @param title the title
     */
    public void updateTitleCategory(int index, String title) {
        if (index >= 0 && index < categories.size()) {
            categories.get(index).setTile(title);
        }
    }

    /**
     * 添加分类.
     * 
     * @param title
     *            the title
     * @param adapter
     *            the adapter
     */
    public void addCategory(String title, Adapter adapter) {
        categories.add(new Category(title, adapter));
    }

    /**
     * 清空分类集合.
     */
    public void clearCategorys() {
        categories.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public int getCount() {
        int total = 0;
        for (Category category : categories) {
            total += category.getAdapter().getCount() + 1;
        }
        return total;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItem(int)
     */
    @Override
    public Object getItem(int position) {
        for (Category category : categories) {
            if (position == 0) {
                return category;
            }
            int size = category.getAdapter().getCount() + 1;
            if (position < size) {
                return category.getAdapter().getItem(position - 1);
            }
            position -= size;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#getViewTypeCount()
     */
    public int getViewTypeCount() {
        int total = 1;
        for (Category category : categories) {
            total += category.getAdapter().getViewTypeCount();
        }
        return total;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.BaseAdapter#getItemViewType(int)
     */
    public int getItemViewType(int position) {
        int typeOffset = 1;
        for (Category category : categories) {
            if (position == 0) {
                return 0;
            }
            int size = category.getAdapter().getCount() + 1;
            if (position < size) {
                return typeOffset + category.getAdapter().getItemViewType(position - 1);
            }
            position -= size;
            typeOffset += category.getAdapter().getViewTypeCount();
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int categoryIndex = 0;
        for (Category category : categories) {
            if (position == 0) {
                return getTitleView(category.getTitle(), categoryIndex, convertView, parent);
            }
            int size = category.getAdapter().getCount() + 1;
            if (position < size) {
                return category.getAdapter().getView(position - 1, convertView, parent);
            }
            position -= size;
            categoryIndex++;
        }
        return null;
    }

    /**
     * Gets the title view.
     * 
     * @param caption
     *            the caption
     * @param index
     *            the index
     * @param convertView
     *            the convert view
     * @param parent
     *            the parent
     * @return the title view
     */
    protected abstract View getTitleView(String caption, int index, View convertView,
            ViewGroup parent);
}