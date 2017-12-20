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

package org.geometerplus.zlibrary.text.model;

class ZLTextParagraphImpl implements ZLTextParagraph {
	private final ZLTextPlainModel myModel;// 书籍的数据源
	private final int myIndex;// 段落索引

	ZLTextParagraphImpl(ZLTextPlainModel model, int index) {
		myModel = model;
		myIndex = index;
	}

	public EntryIterator iterator() {// 书籍实体内容迭代器，在ZLTextPlainModel.EntryIteratorImpl中实现，负责读取书籍内的各种数据
		return myModel.new EntryIteratorImpl(myIndex);
	}

	public byte getKind() {
		return Kind.TEXT_PARAGRAPH;
	}
}
