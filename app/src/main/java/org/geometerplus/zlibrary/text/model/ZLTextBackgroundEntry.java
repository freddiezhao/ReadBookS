package org.geometerplus.zlibrary.text.model;

import org.geometerplus.zlibrary.core.util.ZLColor;

public class ZLTextBackgroundEntry {
	public static final byte BG_KIND_BACKGROUND = 1;
	public static final byte BG_KIND_BORDER = 2;
	public static final byte BG_KIND_BOTH = 3;
	
	private byte myKind;
	private boolean myStart;
	private boolean myBlockType;
	private ZLColor myBgColor;
	
	private ZLColor myLeftBorderColor;
	private ZLColor myTopBorderColor;
	private ZLColor myRightBorderColor;
	private ZLColor myBottomBorderColor;
	
	public ZLTextBackgroundEntry(byte kind, boolean isStart, boolean isBlockType, ZLColor bgColor
			, ZLColor lColor, ZLColor tColor, ZLColor rColor, ZLColor bColor) {
		myKind = kind;
		myStart = isStart;
		myBlockType = isBlockType;
		myBgColor = bgColor;
		
		myLeftBorderColor = lColor;
		myTopBorderColor = tColor;
		myRightBorderColor = rColor;
		myBottomBorderColor = bColor;
	}
	public byte getKind() {
		return myKind;
	}
	public boolean getStart() {
		return myStart;
	}
	public boolean getBlockType() {
		return myBlockType;
	}
	public ZLColor getBgColor() {
		return myBgColor;
	}
	
	public ZLColor getLeftBorderColor() {
		return myLeftBorderColor;
	}
	public ZLColor getTopBorderColor() {
		return myTopBorderColor;
	}
	public ZLColor getRightBorderColor() {
		return myRightBorderColor;
	}
	public ZLColor getBottomBorderColor() {
		return myBottomBorderColor;
	}
}
