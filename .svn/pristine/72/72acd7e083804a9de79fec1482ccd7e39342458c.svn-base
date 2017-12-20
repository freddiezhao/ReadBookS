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

package org.geometerplus.zlibrary.text.view;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.geometerplus.zlibrary.text.model.ZLTextModel;

/** 缓存段落数据{@link ZLTextParagraphCursor} */
class ZLTextParagraphCursorCache
{
	private final static class Key
	{// ourMap的key，遵从数据源，段落索引一致的原则才能取出对应的ZLTextParagraphCursor
		private final ZLTextModel	myModel;	// 书籍的数据源
		private final int			myIndex;	// 实际上是段落的索引，从0开始

		public Key(ZLTextModel model, int index)
		{
			myModel = model;
			myIndex = index;
		}

		// Key相等的唯一条件自然是myModel数据源和该数据源的段落索引都一致了，才能取到对应的ZLTextParagraphCursor缓存数据
		public boolean equals(Object o)
		{
			Key k = (Key) o;
			return (myModel == k.myModel) && (myIndex == k.myIndex);
		}

		public int hashCode()
		{
			return myModel.hashCode() + myIndex;
		}
	}

	private static final HashMap<Key, WeakReference<ZLTextParagraphCursor>>	ourMap	= new HashMap<Key, WeakReference<ZLTextParagraphCursor>>();

	public static void put(ZLTextModel model, int index, ZLTextParagraphCursor cursor)
	{
		ourMap.put(new Key(model, index), new WeakReference<ZLTextParagraphCursor>(cursor));
	}

	public static ZLTextParagraphCursor get(ZLTextModel model, int index)
	{
		WeakReference<ZLTextParagraphCursor> ref = ourMap.get(new Key(model, index));
		return (ref != null) ? ref.get() : null;
	}

	public static void remove(ZLTextModel model, int index)
	{
		Key key = new Key(model, index);
		if (ourMap.containsKey(key)) {
			ourMap.remove(key);
		}
	}

	public static void clear()
	{
		ourMap.clear();
	}
}
