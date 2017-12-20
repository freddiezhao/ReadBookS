package com.sina.book.htmlspanner.handlers;

import org.htmlcleaner.TagNode;

import com.sina.book.htmlspanner.TagNodeHandler;
import com.sina.book.util.Util;

public class ImageHandler extends TagNodeHandler {

    @Override
    public void handleTagNode(TagNode node, StringBuilder builder, int start,
            int end) {
        if (builder.length() > 0
                && builder.charAt(builder.length() - 1) != '\n') {
            builder.append("\n");
        }
        String src = node.getAttributeByName("src");
        if (src != null) {
            src = Util.getPathName(src);
            builder.append("[<(i)>][u=");
            builder.append(src);
            builder.append(";]");
        }
        appendNewLine(builder);
    }

    @Override
    public boolean rendersContent() {
        return true;
    }
}
