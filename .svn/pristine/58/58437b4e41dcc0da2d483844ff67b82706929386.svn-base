/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.ui.android.view;

import org.geometerplus.zlibrary.core.view.ZLView;

import android.graphics.Bitmap;

/**
 * 位图管理器，用来生成绘制在屏幕上的位图
 * 
 * @author chenjl
 * 
 */
class BitmapManager
{
	private final int					SIZE		= 2;							// 缓存页面数量
	private final Bitmap[]				myBitmaps	= new Bitmap[SIZE];			// 页面
	private final ZLView.PageIndex[]	myIndexes	= new ZLView.PageIndex[SIZE];	// 页面索引

	private int							myWidth;
	private int							myHeight;

	private final ZLAndroidWidget		myWidget;

	BitmapManager(ZLAndroidWidget widget)
	{
		myWidget = widget;
	}

	public void clear(){
		for (int i = 0; i < SIZE; ++i) {
			if(myBitmaps[i] != null){
				myBitmaps[i].recycle();
			}
			myBitmaps[i] = null;
			myIndexes[i] = null;
		}
	}
	
	/**
	 * 设置位图宽高
	 * 
	 * @param w
	 * @param h
	 */
	void setSize(int w, int h)
	{
		if (myWidth != w || myHeight != h) {
			myWidth = w;
			myHeight = h;
			// 全部置空
			for (int i = 0; i < SIZE; ++i) {
				if(myBitmaps[i] != null){
					myBitmaps[i].recycle();
				}
				myBitmaps[i] = null;
				myIndexes[i] = null;
			}
			// 执行下垃圾回收，尽可能的回收资源
			System.gc();
			System.gc();
			System.gc();
		}
	}

	/**
	 * 得到绘制的位图
	 * 
	 * @param index
	 *            要绘制的页
	 * @return
	 */
	Bitmap getBitmap(ZLView.PageIndex index)
	{
		// 判断一下有没有现成的，则如果有直接返回
		for (int i = 0; i < SIZE; ++i) {
			if (index == myIndexes[i]) {
				return myBitmaps[i];
			}
		}
		// 获取index代表的索引
		final int iIndex = getInternalIndex(index);
		myIndexes[iIndex] = index;
		// 创建位图
		if (myBitmaps[iIndex] == null) {
			try {
				myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
			} catch (OutOfMemoryError e) {
				System.gc();
				System.gc();
				myBitmaps[iIndex] = Bitmap.createBitmap(myWidth, myHeight, Bitmap.Config.RGB_565);
			}
		}
		// 绘制位图
		myWidget.drawOnBitmap(myBitmaps[iIndex], index);
		return myBitmaps[iIndex];
	}

	/**
	 * 根据myIndexes的值找到对应的myBitmaps的索引
	 * 
	 * @param index
	 * @return
	 */
	private int getInternalIndex(ZLView.PageIndex index)
	{
		for (int i = 0; i < SIZE; ++i) {// 先寻找第一个为空的索引
			if (myIndexes[i] == null) {
				return i;
			}
		}
		for (int i = 0; i < SIZE; ++i) {// 如果未找到，就寻找第一个不是缓存当前页的索引
			if (myIndexes[i] != ZLView.PageIndex.current) {
				return i;
			}
		}
		throw new RuntimeException("That's impossible");
	}

	void reset()
	{
		for (int i = 0; i < SIZE; ++i) {// 复位索引数组
			myIndexes[i] = null;
		}
	}

	// 翻页后,更新索引数组
	void shift(boolean forward)
	{
		for (int i = 0; i < SIZE; ++i) {
			if (myIndexes[i] == null) {
				continue;
			}
			myIndexes[i] = forward ? myIndexes[i].getPrevious() : myIndexes[i].getNext();
		}
	}
}
