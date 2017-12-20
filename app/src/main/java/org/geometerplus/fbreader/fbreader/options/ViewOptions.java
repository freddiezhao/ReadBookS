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

package org.geometerplus.fbreader.fbreader.options;

import org.geometerplus.fbreader.fbreader.FBView;
import org.geometerplus.zlibrary.core.library.ZLibrary;
import org.geometerplus.zlibrary.core.options.ZLBooleanOption;
import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.text.view.style.ZLTextStyleCollection;

/**
 * 配置可绘制区域与屏幕的上下左右边距值，默认颜色配置方案(如字体颜色，背景色等)，<br>
 * 底部电池电量，页码，时间的状态栏高度的类，另外负责初始化{@link ZLTextStyleCollection}。
 * 
 * @author chenjl
 * 
 */
public class ViewOptions
{
	public final ZLBooleanOption		TwoColumnView;
	public final ZLIntegerRangeOption	LeftMargin;
	public final ZLIntegerRangeOption	RightMargin;
	public final ZLIntegerRangeOption	TopMargin;
	public final ZLIntegerRangeOption	BottomMargin;
	public final ZLIntegerRangeOption	SpaceBetweenColumns;
	public final ZLIntegerRangeOption	ScrollbarType;
	public final ZLIntegerRangeOption	FooterHeight;
	public final ZLStringOption			ColorProfileName;

	private ColorProfile				myColorProfile;
	private ZLTextStyleCollection		myTextStyleCollection;
	private FooterOptions				myFooterOptions;

	public ViewOptions()
	{
		final ZLibrary zlibrary = ZLibrary.Instance();

		final int dpi = zlibrary.getDisplayDPI();
		final int x = zlibrary.getWidthInPixels();
		final int y = zlibrary.getHeightInPixels();
		final int horMargin = Math.min(dpi / 5, Math.min(x, y) / 30);

		TwoColumnView =
				new ZLBooleanOption("Options", "TwoColumnView", x * x + y * y >= 42 * dpi * dpi);// 算出是否允许屏幕设置显示两列视图
		LeftMargin =
				new ZLIntegerRangeOption("Options", "LeftMargin", 0, 100, horMargin);// 配置可绘制区域离屏幕左侧的margin边距值
		RightMargin =
				new ZLIntegerRangeOption("Options", "RightMargin", 0, 100, horMargin);// 配置可绘制区域离屏幕右侧的margin边距值
		TopMargin =
				new ZLIntegerRangeOption("Options", "TopMargin", 0, 100, horMargin);// 配置可绘制区域离屏幕顶部的margin边距值
		BottomMargin =
				new ZLIntegerRangeOption("Options", "BottomMargin", 0, 100, horMargin);// 配置可绘制区域离屏幕底部的margin边距值
		SpaceBetweenColumns =
				new ZLIntegerRangeOption("Options", "SpaceBetweenColumns", 0, 300, 5 * horMargin);// 当以横屏两列显示时，配置两列之间的间隙
		ScrollbarType =
				new ZLIntegerRangeOption("Options", "ScrollbarType", 0, 3, FBView.SCROLLBAR_SHOW_AS_FOOTER);
		FooterHeight =
				new ZLIntegerRangeOption("Options", "FooterHeight", 8, dpi / 8, dpi / 14);// 底部栏的高度

		ColorProfileName =
				new ZLStringOption("Options", "ColorProfile", ColorProfile.DAY);// 颜色配置方案，具体数据将在ColorProfile中加载配置
		ColorProfileName.setSpecialName("colorProfile");
	}

	public ColorProfile getColorProfile()
	{
		final String name = ColorProfileName.getValue();
		if (myColorProfile == null || !name.equals(myColorProfile.Name)) {
			myColorProfile = ColorProfile.get(name);
		}
		return myColorProfile;
	}

	public ZLTextStyleCollection getTextStyleCollection()
	{
		if (myTextStyleCollection == null) {
			myTextStyleCollection = new ZLTextStyleCollection("Base");
		}
		return myTextStyleCollection;
	}

	public FooterOptions getFooterOptions()
	{
		if (myFooterOptions == null) {
			myFooterOptions = new FooterOptions();
		}
		return myFooterOptions;
	}
}
