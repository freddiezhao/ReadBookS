package com.sina.book.htmlspanner.handlers;

import org.htmlcleaner.TagNode;

import com.sina.book.htmlspanner.TagNodeHandler;

/**
 * 直接忽略tag的内容
 * 
 * @author Tsimle
 * 
 */
public class IgnoreHandler extends TagNodeHandler {

    @Override
    public void handleTagNode(TagNode node, StringBuilder builder,
            int start, int end) {
        // donothing 忽略内容
    }

    @Override
    public boolean rendersContent() {
        return true;
    }
}
