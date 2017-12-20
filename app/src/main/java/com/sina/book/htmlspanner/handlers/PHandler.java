package com.sina.book.htmlspanner.handlers;

import org.htmlcleaner.TagNode;

import com.sina.book.htmlspanner.TagNodeHandler;

public class PHandler extends TagNodeHandler {

    @Override
    public void handleTagNode(TagNode node, StringBuilder builder, int start,
            int end) {
        // 仅当p中有内容，才添加新行
        if (end > start) {
            appendNewLine(builder);
        }
    }
}
