package org.geometerplus.zlibrary.text.view;

import org.geometerplus.fbreader.bookmodel.FBTextKind;
import org.geometerplus.zlibrary.text.view.style.ZLTextExplicitlyDecoratedStyle;
import org.geometerplus.zlibrary.text.view.style.ZLTextNGStyle;
import org.geometerplus.zlibrary.text.view.style.ZLTextNGStyleDescription;

public class ZLBgUtil
{

	public static ZLBgUtil	i;

	private ZLBgUtil()
	{

	}

	static {
		i = new ZLBgUtil();
	}

	/**
	 * 字体样式
	 */
	private ZLTextStyle	myTextStyle;

	final void setTextStyle(ZLTextStyle style)
	{
		if (myTextStyle != style) {
			myTextStyle = style;
		}
	}

	public ZLTextStyle getTextStyle()
	{
		return myTextStyle;
	}

	void applyStyleChangeElement(ZLTextElement element, ZLTextViewBase base)
	{
		if (element == ZLTextElement.StyleClose) {
			applyStyleClose();
		} else if (element instanceof ZLTextStyleElement) {
			applyStyle((ZLTextStyleElement) element);
		} else if (element instanceof ZLTextControlElement) {
			if (((ZLTextControlElement)element).Kind == FBTextKind.HR) {
				return;
			}
			applyControl((ZLTextControlElement) element, base);
		}
	}

	boolean isStyleChangeElement(ZLTextElement element)
	{
		return element == ZLTextElement.StyleClose
				|| element instanceof ZLTextStyleElement
				|| element instanceof ZLTextControlElement;
	}

	void applyStyleChanges(ZLTextParagraphCursor cursor, int index, int end, ZLTextViewBase base)
	{
		resetTextStyle(base);
		int paragraphLength = cursor.getParagraphLength();
		for (; index != end && index < paragraphLength; ++index) {
			ZLTextElement element = cursor.getElement(index);
			if (!isStyleChangeElement(element)) {
				break;
			}
			applyStyleChangeElement(element, base);
		}
	}

	void applyStyleChanges(ZLTextParagraphCursor cursor, ZLTextViewBase base)
	{
		resetTextStyle(base);
		int currentElementIndex = 0;
		int paragraphLength = cursor.getParagraphLength();
		ZLTextElement element = cursor.getElement(currentElementIndex);
		while (isStyleChangeElement(element)) {
			applyStyleChangeElement(element, base);
			++currentElementIndex;
			if (currentElementIndex == paragraphLength) {
				break;
			}
			element = cursor.getElement(currentElementIndex);
		}
	}

	void applyStyleChangesAll(ZLTextParagraphCursor cursor, ZLTextViewBase base)
	{
		resetTextStyle(base);
		int currentElementIndex = 0;
		int paragraphLength = cursor.getParagraphLength();
		ZLTextElement element = cursor.getElement(currentElementIndex);
		for (; currentElementIndex < paragraphLength; ++currentElementIndex) {
			if (isStyleChangeElement(element)) {
				applyStyleChangeElement(element, base);
			}
			element = cursor.getElement(currentElementIndex);
		}
	}

	private void applyStyle(ZLTextStyleElement element)
	{
		setTextStyle(new ZLTextExplicitlyDecoratedStyle(myTextStyle,
				element.Entry));
	}

	private void applyStyleClose()
	{
		setTextStyle(myTextStyle.Parent);
	}

	private void applyControl(ZLTextControlElement control, ZLTextViewBase base)
	{
		if (control.IsStart) {
			final ZLTextHyperlink hyperlink = control instanceof ZLTextHyperlinkControlElement ? ((ZLTextHyperlinkControlElement) control).Hyperlink
					: null;
			final ZLTextNGStyleDescription description = base.getTextStyleCollection()
					.getDescription(control.Kind);
			if (description != null) {
				setTextStyle(new ZLTextNGStyle(myTextStyle, description,
						hyperlink));
			}
		} else {
			setTextStyle(myTextStyle.Parent);
		}
	}

	final void resetTextStyle(ZLTextViewBase base)
	{
		setTextStyle(base.getTextStyleCollection().getBaseStyle());
	}

}
