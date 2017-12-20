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

package org.geometerplus.zlibrary.ui.android.image;

import org.geometerplus.zlibrary.core.image.ZLImageData;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

/**
 * 图片数据
 * 
 * @author chenjl
 * 
 */
public abstract class ZLAndroidImageData implements ZLImageData {

	/**
	 * 源位图
	 */
	private Bitmap myBitmap;
	/**
	 * 原始宽?
	 */
	private int myRealWidth;
	/**
	 * 原始高?
	 */
	private int myRealHeight;
	/**
	 * 最终请求的宽高
	 */
	private ZLPaintContext.Size myLastRequestedSize = null;
	/**
	 * 最终请求的图片缩放类型
	 */
	private ZLPaintContext.ScalingType myLastRequestedScaling = ZLPaintContext.ScalingType.OriginalSize;

	protected ZLAndroidImageData() {
	}

	protected abstract Bitmap decodeWithOptions(BitmapFactory.Options options);

	public Bitmap getFullSizeBitmap() {
		return getBitmap(null, ZLPaintContext.ScalingType.OriginalSize);
	}

	public Bitmap getBitmap(int maxWidth, int maxHeight) {
		return getBitmap(new ZLPaintContext.Size(maxWidth, maxHeight),
				ZLPaintContext.ScalingType.FitMaximum);
	}

	public synchronized Bitmap getBitmap(ZLPaintContext.Size maxSize,
			ZLPaintContext.ScalingType scaling) {
		if (scaling != ZLPaintContext.ScalingType.OriginalSize) {
			if (maxSize == null || maxSize.Width <= 0 || maxSize.Height <= 0) {
				return null;
			}
		}
		if (maxSize == null) {
			maxSize = new ZLPaintContext.Size(-1, -1);
		}
		if (!maxSize.equals(myLastRequestedSize)
				|| scaling != myLastRequestedScaling) {//只有maxSize或缩放类型发生变化才重新计算
			myLastRequestedSize = maxSize;
			myLastRequestedScaling = scaling;

			if (myBitmap != null) {
				try{
					myBitmap.recycle();
					myBitmap = null;
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			try {
				final BitmapFactory.Options options = new BitmapFactory.Options();
				// 先计算出位图的原始大小
				if (myRealWidth <= 0) {
					options.inJustDecodeBounds = true;
					decodeWithOptions(options);
					myRealWidth = options.outWidth;
					myRealHeight = options.outHeight;
				}
				options.inJustDecodeBounds = false;
				options.inPreferredConfig = Config.RGB_565;
				// 缩放系数
				int coefficient = 1;
				if (scaling == ZLPaintContext.ScalingType.IntegerCoefficient) {//整数倍缩放
					if (myRealHeight > maxSize.Height
							|| myRealWidth > maxSize.Width) {//需要缩小图片
						coefficient =  Math.max((myRealHeight - 1)
								/ maxSize.Height, (myRealWidth - 1)
								/ maxSize.Width);
					}
				}
				options.inSampleSize = coefficient;
				// 根据新的缩放系数重新生成myBitmap
				myBitmap = decodeWithOptions(options);
				if (myBitmap != null) {
					switch (scaling) {
					case OriginalSize:
						break;
					case FitMaximum: {
						// 判断得到的位图是否符合要求，不符合继续缩放
						final int bWidth = myBitmap.getWidth();
						final int bHeight = myBitmap.getHeight();
						if (bWidth > 0 && bHeight > 0
								&& bWidth != maxSize.Width
								&& bHeight != maxSize.Height) {
							final int w, h;
							if (bWidth * maxSize.Height > bHeight
									* maxSize.Width) {
								w = maxSize.Width;
								h = Math.max(1, bHeight * w / bWidth);
							} else {
								h = maxSize.Height;
								w = Math.max(1, bWidth * h / bHeight);
							}
							final Bitmap scaled = Bitmap.createScaledBitmap(
									myBitmap, w, h, false);
							if (scaled != null) {
								myBitmap = scaled;
							}
						}
						break;
					}
					case IntegerCoefficient: {//IntegerCoefficient目前共在三处使用，获得ImageView宽高和绘制图片时，也就是正文的图片，因此在此分支上修改只影响正文图片
						// 判断得到的位图是否符合要求，不符合继续缩放
						final int bWidth = myBitmap.getWidth();
						final int bHeight = myBitmap.getHeight();
						if (bWidth > 0
								&& bHeight > 0
								&& (bWidth > maxSize.Width || bHeight > maxSize.Height)) {//经过一次整数倍缩小后，bitmap宽或高仍大于maxSize的宽或高才缩小，否则直接居中显示
							final int w, h;
							if (bWidth * maxSize.Height > bHeight
									* maxSize.Width) {
								w = maxSize.Width;
								h = Math.max(1, bHeight * w / bWidth);//根据宽计算高度
							} else {
								h = maxSize.Height;
								w = Math.max(1, bWidth * h / bHeight);
							}
							final Bitmap scaled = Bitmap.createScaledBitmap(
									myBitmap, w, h, false);
							if (scaled != null) {
								myBitmap = scaled;
							}
						}
						break;
					}
					}
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			}
		}
		return myBitmap;
	}
	
	public void clear(){
		if(myBitmap != null){
			try{
				myBitmap.recycle();
				myBitmap = null;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
