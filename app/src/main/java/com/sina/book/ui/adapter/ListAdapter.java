package com.sina.book.ui.adapter;

import java.util.List;

import android.widget.BaseAdapter;

import com.sina.book.data.ConstantData;

/**
 * adapter中包含一个list，有更多数据 可以继承该类
 * 
 * @author Tsimle
 * 
 * @param <T>
 */
public abstract class ListAdapter<T> extends BaseAdapter
{

	protected List<T>	mDataList;
	protected boolean	mIsAdding		= false;
	protected int		mTotal;

	// 新添加字段
	protected int		mPerPage		= ConstantData.PAGE_SIZE;	// 默认20
	protected int		mCurrentPage	= 1;						// 默认第一页
	protected int		mTotalPage;

	public void setPerPage(int perPage)
	{
		mPerPage = perPage;
		setPerPage(perPage, true);
	}

	public void setPerPage(int perPage, boolean updateTotalPage)
	{
		mPerPage = perPage;
		if (updateTotalPage)
			setTotal(getTotal());
	}

	public int getPerPage()
	{
		return mPerPage;
	}

	public void setCurrentPage(int currPage)
	{
		mCurrentPage = currPage;
	}

	public int getCurrentPage()
	{
		return mCurrentPage;
	}

	public void setTotalPage(int totalPage)
	{
		mTotalPage = totalPage;
	}

	public int getTotalPage()
	{
		return mTotalPage;
	}

	@Override
	public int getCount()
	{
		int count = 0;
		if (mDataList != null) {
			count = mDataList.size();
		}
		return count;
	}

	@Override
	public Object getItem(int position)
	{
		if (position < mDataList.size()) {
			return mDataList.get(position);
		} else {
			return null;
		}
	}

	@Override
	public long getItemId(int position)
	{
		if (position == mDataList.size()) {
			return 0;
		}
		return position;
	}

	/**
	 * 设置数据列表
	 * 
	 * @param datalist
	 */
	public void setList(List<T> datalist)
	{
		if (mDataList == null) {
			mDataList = createList();
		}

		if (datalist == null) {
			return;
		}

		if (mDataList.size() > 0) {
			mDataList.clear();
		}
		mDataList.addAll(datalist);
	}

	/**
	 * 添加数据列表
	 * 
	 * @param datalist
	 */
	public void addList(List<T> datalist)
	{
		if (mDataList == null) {
			mDataList = createList();
		}
		if (datalist != null) {
			mDataList.addAll(datalist);
		}

		mIsAdding = false;
	}

	/**
	 * 设置数据总数
	 * 
	 * @param total
	 */
	public void setTotal(int total)
	{
		mTotal = total;
		if (total > mPerPage) {
			// 更新总页数
			int temp = mTotal % mPerPage;
			int totalPage = mTotal / mPerPage;
			if (temp != 0)
				totalPage++;
			setTotalPage(totalPage);
		} else {
			setTotalPage(1);
		}
	}

	public void setTotalAndPerpage(int total, int perpage)
	{
		mTotal = total;
		mPerPage = perpage;
		if (total > mPerPage) {
			// 更新总页数
			int temp = mTotal % mPerPage;
			int totalPage = mTotal / mPerPage;
			if (temp != 0)
				totalPage++;
			setTotalPage(totalPage);
		} else {
			setTotalPage(1);
		}
	}

	public int getTotal()
	{
		return mTotal;
	}

	/**
	 * 获得当前的数据数目
	 * 
	 * @return
	 */
	public int getDataSize()
	{
		int size = 0;
		if (mDataList != null) {
			size = mDataList.size();
		}
		return size;
	}

	/**
	 * 是否正在获取更多
	 * 
	 * @return
	 */
	public boolean IsAdding()
	{
		return mIsAdding;
	}

	/**
	 * 设置获取更多标志
	 * 
	 * @param isAdding
	 */
	public void setAdding(boolean isAdding)
	{
		this.mIsAdding = isAdding;
	}

	/**
	 * 是否还有更多的数据
	 * 
	 * @return
	 */
	public boolean hasMore()
	{
		// if (mDataList != null && mDataList.size() < mTotal) {
		// return true;
		// }
		// 最后一页时无更多
		if (mCurrentPage >= mTotalPage) {
			return false;
		}
		return true;
	}

	/**
	 * 是否还有更多的数据
	 * 
	 * @return
	 */
	public boolean hasMore2()
	{
		if (mDataList != null && mDataList.size() < mTotal) {
			return true;
		}
		return false;
	}

	protected abstract List<T> createList();
}
