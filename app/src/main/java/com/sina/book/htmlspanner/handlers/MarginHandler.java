/*
 * Copyright (C) 2011 Alex Kuiper <http://www.nightwhistler.net>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sina.book.htmlspanner.handlers;

import org.htmlcleaner.TagNode;

import com.sina.book.htmlspanner.TagNodeHandler;

/**
 * Applies margin-formatting, indenting text to the right.
 * 
 * @author Alex Kuiper
 *
 */
public class MarginHandler extends TagNodeHandler {

//	private static int MARGIN_INDENT = 30;
	
	@Override
	public void beforeChildren(TagNode node,
	        StringBuilder builder) {

		if (builder.length() > 0
				&& builder.charAt(builder.length() - 1) != '\n') {
			appendNewLine(builder);
		}
	}

	public void handleTagNode(TagNode node,
			StringBuilder builder, int start, int end) {
		appendNewLine(builder);
		appendNewLine(builder);
	}
}
