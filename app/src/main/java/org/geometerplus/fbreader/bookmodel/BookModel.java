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

package org.geometerplus.fbreader.bookmodel;

import java.util.Arrays;
import java.util.List;

import org.geometerplus.zlibrary.core.fonts.*;
import org.geometerplus.zlibrary.text.model.*;

import org.geometerplus.android.util.ZLog;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.fbreader.formats.BuiltinFormatPlugin;
import org.geometerplus.fbreader.formats.FormatPlugin;

public abstract class BookModel
{

	/**
	 * 根据book获取FormatPlugin，根据插件，判断出属于哪种格式<br>
	 * ，然后先判断是否采用本地C生成Model还是Java生成Model，然后<br>
	 * 再使用插件对Model对象进行初始化。
	 * 
	 * @param book
	 * @return
	 * @throws BookReadingException
	 */
	public static BookModel createModel(Book book) throws BookReadingException
	{
		final FormatPlugin plugin = book.getPlugin();

		System.err.println("using plugin: " + plugin.supportedFileType() + "/" + plugin.type());

		final BookModel model;
		switch (plugin.type()) {
		case NATIVE:
			// TODO:
			model = new NativeBookModel(book);
			break;

		case JAVA:
			model = new JavaBookModel(book);
			break;
		default:
			throw new BookReadingException(
					"unknownPluginType", null, new String[] { plugin.type().toString() });
		}

		((BuiltinFormatPlugin) plugin).readModel(model);
		return model;
	}

	public final Book			Book;
	public final TOCTree		TOCTree		= new TOCTree();
	public final FontManager	FontManager	= new FontManager();

	public static final class Label
	{
		public final String	ModelId;
		public final int	ParagraphIndex;

		public Label(String modelId, int paragraphIndex)
		{
			ModelId = modelId;
			ParagraphIndex = paragraphIndex;
		}
	}

	protected BookModel(Book book)
	{
		Book = book;
	}

	public abstract ZLTextModel getTextModel();

	public abstract ZLTextModel getFootnoteModel(String id);

	protected abstract Label getLabelInternal(String id);

	public interface LabelResolver
	{
		List<String> getCandidates(String id);
	}

	private LabelResolver	myResolver;

	public void setLabelResolver(LabelResolver resolver)
	{
		myResolver = resolver;
	}

	public Label getLabel(String id)
	{
		Label label = getLabelInternal(id);
		if (label == null && myResolver != null) {
			for (String candidate : myResolver.getCandidates(id)) {
				label = getLabelInternal(candidate);
				if (label != null) {
					break;
				}
			}
		}
		return label;
	}

	/**
	 * 添加native解析出的CSS中得字体类型<br>
	 * 由NativeFormatPlugin:readModel() -> readModelNative() 从底层调用该方法<br>
	 * 底层代码会将所有解析到的字体组依次调用传入FontManager，如传入的families<br>
	 * 在一次调试过程中的值为(调试的书籍为南方周末20140103_4717491b.epub)：<br>
	 * [zw, 宋体, 明体, 明朝, serif]、[kt, 楷体, 楷体_gb2312, zw, serif]、[ht, 微软雅黑, 黑体, zw,
	 * sans-serif]、<br>
	 * [ht, 微软雅黑, 黑体, MYing Hei S, MYing Hei T, TBGothic, zw, sans-serif]...<br>
	 * 
	 * @param families
	 */
	public void registerFontFamilyList(String[] families)
	{
		FontManager.index(Arrays.asList(families));
		ZLog.d(ZLog.ZLDebug, "Font Information >> families:" + families);
	}

	public void registerFontEntry(String family, FontEntry entry)
	{
		FontManager.Entries.put(family, entry);
	}

	public void registerFontEntry(String family, FileInfo normal, FileInfo bold, FileInfo italic, FileInfo boldItalic)
	{
		registerFontEntry(family, new FontEntry(family, normal, bold, italic, boldItalic));
	}
}
