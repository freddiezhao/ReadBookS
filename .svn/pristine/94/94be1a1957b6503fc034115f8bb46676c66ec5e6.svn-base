package com.sina.book.htmlspanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.ContentNode;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import com.sina.book.htmlspanner.handlers.HeaderHandler;
import com.sina.book.htmlspanner.handlers.ImageHandler;
import com.sina.book.htmlspanner.handlers.ListItemHandler;
import com.sina.book.htmlspanner.handlers.MarginHandler;
import com.sina.book.htmlspanner.handlers.NewLineHandler;
import com.sina.book.htmlspanner.handlers.PHandler;
import com.sina.book.htmlspanner.handlers.PreHandler;

/**
 * HtmlSpanner provides an alternative to Html.fromHtml() from the Android
 * libraries.
 * 
 * In its simplest form, just call new HtmlSpanner().fromHtml() to get a similar
 * result. The real strength is in being able to register custom NodeHandlers.
 * 
 * @author MarkMjw
 * 
 */
public class HtmlSpanner {
    // private static final String TAG = "HtmlSpanner";

    private Map<String, TagNodeHandler> handlers;

    private boolean stripExtraWhiteSpace = false;

    // private static Pattern SPECIAL_CHAR =
    // Pattern.compile("(\t| +|&[a-z]*;|&#[0-9]*;|\n)");
    //
    // private static Map<String, String> REPLACEMENTS = new HashMap<String,
    // String>();

    private HtmlCleaner htmlCleaner;

    // static {
    //
    // REPLACEMENTS.put("", " ");
    // REPLACEMENTS.put("\n", " ");
    // REPLACEMENTS.put("&nbsp;", " ");
    // REPLACEMENTS.put("&amp;", "&");
    // REPLACEMENTS.put("&quot;", "\"");
    // REPLACEMENTS.put("&cent;", "¢");
    // REPLACEMENTS.put("&lt;", "<");
    // REPLACEMENTS.put("&gt;", ">");
    // REPLACEMENTS.put("&sect;", "§");
    //
    // }

    /**
     * Creates a new HtmlSpanner using a default HtmlCleaner instance.
     */
    public HtmlSpanner(boolean isBody) {
        this(createHtmlCleaner(isBody));
    }

    /**
     * Creates a new HtmlSpanner using the given HtmlCleaner instance.
     * 
     * This allows for a custom-configured HtmlCleaner.
     * 
     * @param cleaner
     */
    public HtmlSpanner(HtmlCleaner cleaner) {
        this.htmlCleaner = cleaner;
        this.handlers = new HashMap<String, TagNodeHandler>();
        registerBuiltInHandlers();
    }

    /**
     * Switch to specify whether excess whitespace should be stripped from the
     * input.
     * 
     * @param stripExtraWhiteSpace
     */
    public void setStripExtraWhiteSpace(boolean stripExtraWhiteSpace) {
        this.stripExtraWhiteSpace = stripExtraWhiteSpace;
    }

    /**
     * Returns if whitespace is being stripped.
     * 
     * @return
     */
    public boolean isStripExtraWhiteSpace() {
        return stripExtraWhiteSpace;
    }

    /**
     * Registers a new custom TagNodeHandler.
     * 
     * If a TagNodeHandler was already registered for the specified tagName it
     * will be overwritten.
     * 
     * @param tagName
     * @param handler
     */
    public void registerHandler(String tagName, TagNodeHandler handler) {
        this.handlers.put(tagName, handler);
        handler.setSpanner(this);
    }

    /**
     * Parses the text in the given String.
     * 
     * @param html
     * 
     * @return a Spanned version of the text.
     */
    public StringBuilder fromHtml(String html) {
        return fromTagNode(this.htmlCleaner.clean(html));
    }

    /**
     * Parses the text in the given Reader.
     * 
     * @param reader
     * @return
     * @throws IOException
     */
    public StringBuilder fromHtml(Reader reader) throws IOException {
        return fromTagNode(this.htmlCleaner.clean(reader));
    }

    /**
     * Parses the text in the given InputStream.
     * 
     * @param inputStream
     * @return
     * @throws IOException
     */
    public StringBuilder fromHtml(InputStream inputStream) throws IOException {
        return fromTagNode(this.htmlCleaner.clean(inputStream));
    }

    /**
     * Gets the currently registered handler for this tag.
     * 
     * Used so it can be wrapped.
     * 
     * @param tagName
     * @return the registed TagNodeHandler, or null if none is registered.
     */
    public TagNodeHandler getHandlerFor(String tagName) {
        return this.handlers.get(tagName);
    }

    /**
     * Creates spanned text from a TagNode.
     * 
     * @param node
     * @return
     */
    public StringBuilder fromTagNode(TagNode node) {
        StringBuilder result = new StringBuilder();
        handleContent(result, node, null);
        return result;
    }

    private static HtmlCleaner createHtmlCleaner(boolean isBody) {
        HtmlCleaner result = new HtmlCleaner();
        CleanerProperties cleanerProperties = result.getProperties();

        cleanerProperties.setAdvancedXmlEscape(true);

        cleanerProperties.setOmitXmlDeclaration(true);
        cleanerProperties.setOmitDoctypeDeclaration(false);

        cleanerProperties.setTranslateSpecialEntities(true);
        cleanerProperties.setTransResCharsToNCR(true);
        cleanerProperties.setRecognizeUnicodeChars(true);

        cleanerProperties.setIgnoreQuestAndExclam(true);
        cleanerProperties.setUseEmptyElementTags(false);

        if (isBody) {
            cleanerProperties.setPruneTags("script,style,head");
        } else {
            cleanerProperties.setPruneTags("script,style,body");
        }
        return result;
    }

    //
    // @SuppressWarnings("unused")
    // private static String getEditedText(String aText) {
    // StringBuffer result = new StringBuffer();
    // Matcher matcher = SPECIAL_CHAR.matcher(aText);
    //
    // while (matcher.find()) {
    // matcher.appendReplacement(result, getReplacement(matcher));
    // }
    // matcher.appendTail(result);
    // return result.toString();
    // }
    //
    // private static String getReplacement(Matcher aMatcher) {
    //
    // String match = aMatcher.group(0).trim();
    // String result = REPLACEMENTS.get(match);
    //
    // if (result != null) {
    // return result;
    // } else if (match.startsWith("&#")) {
    // // Translate to unicode character.
    // try {
    // Integer code = Integer.parseInt(match.substring(2, match.length() - 1));
    // return "" + (char) code.intValue();
    // } catch (NumberFormatException nfe) {
    // return "";
    // }
    // } else {
    // return "";
    // }
    // }

    private void handleContent(StringBuilder builder, Object node,
            TagNode parent) {
        if (node instanceof ContentNode) {

            ContentNode contentNode = (ContentNode) node;

            if (builder.length() > 0) {
                char lastChar = builder.charAt(builder.length() - 1);
                if (lastChar != ' ' && lastChar != '\n') {
                    builder.append(' ');
                }
            }
            // String text =
            // getEditedText(contentNode.getContent().toString()).trim();
            String text = HtmlDecoder.decode(contentNode.getContent()
                    .toString());
            if (null != text) {
                text = text.trim();
                if (!"".equals(text)) {
                    builder.append("\t");
                    builder.append(text);
                }
            }
        } else if (node instanceof TagNode) {
            applySpan(builder, (TagNode) node);
        }
    }

    private void applySpan(StringBuilder builder, TagNode node) {
        TagNodeHandler handler = this.handlers.get(node.getName());
        int lengthBefore = builder.length();

        if (handler != null) {
            handler.beforeChildren(node, builder);
        }

        if (handler == null || !handler.rendersContent()) {

            for (Object childNode : node.getChildren()) {
                handleContent(builder, childNode, node);
            }
        }

        int lengthAfter = builder.length();

        if (handler != null) {
            handler.handleTagNode(node, builder, lengthBefore, lengthAfter);
        }
    }

    private void registerBuiltInHandlers() {

        TagNodeHandler marginHandler = new MarginHandler();

        registerHandler("blockquote", marginHandler);
        registerHandler("ul", marginHandler);
        registerHandler("ol", marginHandler);

        TagNodeHandler brHandler = new NewLineHandler(1);

        registerHandler("br", brHandler);

        TagNodeHandler pHandler = new NewLineHandler(1);

        registerHandler("div", pHandler);
        registerHandler("p", new PHandler());

        registerHandler("h1", new HeaderHandler(1.3f));
        registerHandler("h2", new HeaderHandler(1.2f));
        registerHandler("h3", new HeaderHandler(1.2f));
        registerHandler("h4", new HeaderHandler(1.1f));
        registerHandler("h5", new HeaderHandler(1.1f));
        registerHandler("h6", new HeaderHandler(1f));

        TagNodeHandler preHandler = new PreHandler();

        registerHandler("pre", preHandler);

        registerHandler("li", new ListItemHandler());

        registerHandler("img", new ImageHandler());
    }
}
