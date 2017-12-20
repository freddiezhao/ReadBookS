/*
 * Copyright (C) 2011-2014 Geometer Plus <contact@geometerplus.com>
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

import org.geometerplus.android.util.ZLog;
import org.geometerplus.fbreader.book.Book;
import org.geometerplus.zlibrary.text.model.CachedCharStorageRO;
import org.geometerplus.zlibrary.text.model.ZLTextModel;
import org.geometerplus.zlibrary.text.model.ZLTextNativeModel;

public class NativeBookModel extends BookModelImpl
{
	/**
	 * 数据源，底层代码解析出并返回
	 */
	private ZLTextModel	myBookTextModel;

	NativeBookModel(Book book)
	{
		super(book);
	}

	/**
	 * 由NativeFormatPlugin:readModel() -> readModelNative() 从底层调用该方法<br>
	 * 例如，directoryName为/storage/emulated/0/Android/data/com.sina.book/cache，<br>
	 * fileExtension为nlinks，blocksNumber为1<br>
	 * 
	 * @param directoryName
	 * @param fileExtension
	 * @param blocksNumber
	 */
	public void initInternalHyperlinks(String directoryName, String fileExtension, int blocksNumber)
	{
		myInternalHyperlinks = new CachedCharStorageRO(directoryName, fileExtension, blocksNumber);
	}

	private TOCTree	myCurrentTree	= TOCTree;

	/**
	 * 由NativeFormatPlugin:readModel() ->
	 * readModelNative()从底层调用该方法，该方法传入的text是这本书籍的目录信息
	 * 
	 * @param text
	 * @param reference
	 */
	public void addTOCItem(String text, int reference)
	{
		myCurrentTree = new TOCTree(myCurrentTree);
		ZLog.d(ZLog.ZLDebug, "Catagory Information >> text:" + text + "{" + reference + "}");
		myCurrentTree.setText(text);
		myCurrentTree.setReference(myBookTextModel, reference);
	}

	public void leaveTOCItem()
	{
		myCurrentTree = myCurrentTree.Parent;
		if (myCurrentTree == null) {
			myCurrentTree = TOCTree;
		}
	}

	/**
	 * 由NativeFormatPlugin:readModel() -> readModelNative() 从底层调用该方法
	 * 
	 * @param id
	 * @param language
	 * @param paragraphsNumber
	 * @param entryIndices
	 * @param entryOffsets
	 * @param paragraphLenghts
	 * @param textSizes
	 * @param paragraphKinds
	 * @param directoryName
	 * @param fileExtension
	 * @param blocksNumber
	 * @return
	 */
	public ZLTextModel createTextModel(
			String id, String language, int paragraphsNumber,
			int[] entryIndices, int[] entryOffsets,
			int[] paragraphLenghts, int[] paragraphStartIndexes, int[] textSizes, byte[] paragraphKinds,
			String directoryName, String fileExtension, int blocksNumber
			)
	{
		return new ZLTextNativeModel(
				id, language, paragraphsNumber,
				entryIndices, entryOffsets,
				paragraphLenghts, paragraphStartIndexes, textSizes, paragraphKinds,
				directoryName, fileExtension, blocksNumber, myImageMap, FontManager);
	}

	/**
	 * 由NativeFormatPlugin:readModel() -> readModelNative() 从底层调用该方法<br>
	 * 某epub资源时，这个model对象为ZLTextNativeModel类的实例<br>
	 * 所有的资源信息都在这个model对象中了，由本地代码解析返回的<br>
	 * 
	 * @param model
	 */
	public void setBookTextModel(ZLTextModel model)
	{
		myBookTextModel = model;
	}

	public void setFootnoteModel(ZLTextModel model)
	{
		myFootnotes.put(model.getId(), model);
	}

	@Override
	public ZLTextModel getTextModel()
	{
		return myBookTextModel;
	}

	@Override
	public ZLTextModel getFootnoteModel(String id)
	{
		return myFootnotes.get(id);
	}
}
