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

import java.util.*;

import org.geometerplus.zlibrary.core.util.ZLColor;
import org.geometerplus.zlibrary.core.options.*;
import org.geometerplus.zlibrary.core.view.ZLPaintContext;

public class ColorProfile {
	public static final String DAY = "defaultLight";
	public static final String NIGHT = "defaultDark";

	private static final ArrayList<String> ourNames = new ArrayList<String>();
	private static final HashMap<String,ColorProfile> ourProfiles = new HashMap<String,ColorProfile>();

	public static List<String> names() {
		if (ourNames.isEmpty()) {
			final int size = new ZLIntegerOption("Colors", "NumberOfSchemes", 0).getValue();
			if (size == 0) {
				ourNames.add(DAY);
				ourNames.add(NIGHT);
			} else for (int i = 0; i < size; ++i) {
				ourNames.add(new ZLStringOption("Colors", "Scheme" + i, "").getValue());
			}
		}
		return Collections.unmodifiableList(ourNames);
	}

	public static ColorProfile get(String name) {
		ColorProfile profile = ourProfiles.get(name);
		if (profile == null) {
			profile = new ColorProfile(name);
			ourProfiles.put(name, profile);
		}
		return profile;
	}
	
	public final String Name;
	
	public final ZLStringOption WallpaperOption;
	public final ZLEnumOption<ZLPaintContext.FillMode> FillModeOption;
	public final ZLColorOption BackgroundOption;
	public final ZLColorOption SelectionBackgroundOption;
	public final ZLColorOption SelectionForegroundOption;
	public final ZLColorOption HighlightingOption;
	
	// 普通文本颜色
	public final ZLColorOption RegularTextOption;
	
	// 超链接文本颜色
	public final ZLColorOption HyperlinkTextOption;
	
	public final ZLColorOption VisitedHyperlinkTextOption;
	public final ZLColorOption FooterFillOption;

	private ColorProfile(String name, ColorProfile base) {
		this(name);
		WallpaperOption.setValue(base.WallpaperOption.getValue());
		FillModeOption.setValue(base.FillModeOption.getValue());
		BackgroundOption.setValue(base.BackgroundOption.getValue());
		SelectionBackgroundOption.setValue(base.SelectionBackgroundOption.getValue());
		SelectionForegroundOption.setValue(base.SelectionForegroundOption.getValue());
		HighlightingOption.setValue(base.HighlightingOption.getValue());
		RegularTextOption.setValue(base.RegularTextOption.getValue());
		HyperlinkTextOption.setValue(base.HyperlinkTextOption.getValue());
		VisitedHyperlinkTextOption.setValue(base.VisitedHyperlinkTextOption.getValue());
		FooterFillOption.setValue(base.FooterFillOption.getValue());
	}

	private static ZLColorOption createOption(String profileName, String optionName, int r, int g, int b) {
		return new ZLColorOption("Colors", profileName + ':' + optionName, new ZLColor(r, g, b));
	}

	private ColorProfile(String name) {
		Name = name;
		if (NIGHT.equals(name)) {
			WallpaperOption =
				new ZLStringOption("Colors", name + ":Wallpaper", "");
			FillModeOption =
				new ZLEnumOption<ZLPaintContext.FillMode>(name, "FillMode", ZLPaintContext.FillMode.tile);
			
//			BackgroundOption =
//				createOption(name, "Background", 247, 244, 240);
			BackgroundOption =
					createOption(name, "Background", 44, 44, 44);
			SelectionBackgroundOption =
				createOption(name, "SelectionBackground", 82, 131, 194);
			SelectionForegroundOption =
				createOption(name, "SelectionForeground", 255, 255, 220);
			HighlightingOption =
				createOption(name, "Highlighting", 96, 96, 128);
			
			// 普通文字颜色
			RegularTextOption =
				createOption(name, "Text", 166, 166, 166);
			
			// 超链接文字颜色
			HyperlinkTextOption =
				createOption(name, "Hyperlink", 0, 142, 0);
//			HyperlinkTextOption =
//					createOption(name, "Hyperlink", 60, 142, 224);
			
			VisitedHyperlinkTextOption =
				createOption(name, "VisitedHyperlink", 200, 139, 255);
			FooterFillOption =
				createOption(name, "FooterFillOption", 85, 85, 85);
		} else {
			WallpaperOption =
				new ZLStringOption("Colors", name + ":Wallpaper", "");
//			WallpaperOption =
//					new ZLStringOption("Colors", name + ":Wallpaper", "wallpapers/sepia.jpg");
			FillModeOption =
				new ZLEnumOption<ZLPaintContext.FillMode>(name, "FillMode", ZLPaintContext.FillMode.tile);
			BackgroundOption =
				createOption(name, "Background", 255, 255, 255);
			SelectionBackgroundOption =
				createOption(name, "SelectionBackground", 82, 131, 194);
			SelectionForegroundOption =
				createOption(name, "SelectionForeground", 255, 255, 220);
			HighlightingOption =
				createOption(name, "Highlighting", 255, 192, 128);
			
			// 普通文字颜色
			RegularTextOption =
				createOption(name, "Text", 0, 0, 0);
			// 超链接文字颜色
			HyperlinkTextOption =
				createOption(name, "Hyperlink", 0, 139, 0);
//			HyperlinkTextOption =
//					createOption(name, "Hyperlink", 60, 139, 255);
			
			VisitedHyperlinkTextOption =
				createOption(name, "VisitedHyperlink", 200, 139, 255);
			FooterFillOption =
				createOption(name, "FooterFillOption", 170, 170, 170);
		}
	}
}
