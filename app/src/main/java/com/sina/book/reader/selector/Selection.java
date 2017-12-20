package com.sina.book.reader.selector;

/**
 * 选择器模型
 * 
 * @author MarkMjw
 * @date 2013-2-28
 */
public class Selection {
	/** 默认值 */
	public static final int DEFAULT_VALUE = -1;

	/** 段落索引. */
	private int mParaIndex = DEFAULT_VALUE;

	/** 选择的位置. */
	private int mSelection = DEFAULT_VALUE;

	/** 选择位置X坐标. */
	private float mSelectionX = DEFAULT_VALUE;

	/** 选择位置Y坐标. */
	private float mSelectionY = DEFAULT_VALUE;

	/** 选择字符宽度 */
	private float mSelectionCharWidth = 0;

	/** 选择字符高度 */
	private float mSelectionCharHeight = 0;

	public int getSelection() {
		return mSelection;
	}

	public void setSelection(int selection) {
		this.mSelection = selection;
	}

	public float getSelectionX() {
		return mSelectionX;
	}

	public void setSelectionX(float selectionX) {
		this.mSelectionX = selectionX;
	}

	public float getSelectionY() {
		return mSelectionY;
	}

	public void setSelectionY(float selectionY) {
		this.mSelectionY = selectionY;
	}

	public float getSelectionCharW() {
		return mSelectionCharWidth;
	}

	public void setSelectionCharW(float width) {
		this.mSelectionCharWidth = width;
	}

	public float getSelectionCharH() {
		return mSelectionCharHeight;
	}

	public void setSelectionCharH(float height) {
		this.mSelectionCharHeight = height;
	}

	public int getParaIndex() {
		return mParaIndex;
	}

	public void setParaIndex(int paraIndex) {
		this.mParaIndex = paraIndex;
	}

	public void clearSelection() {
		mParaIndex = DEFAULT_VALUE;
		mSelection = DEFAULT_VALUE;
		mSelectionX = DEFAULT_VALUE;
		mSelectionY = DEFAULT_VALUE;
		mSelectionCharWidth = 0;
		mSelectionCharHeight = 0;
	}

	/**
	 * 比较两个选择位置的大小
	 * 
	 * @param select1
	 * @param select2
	 * @return return 1 if select1 > select2, return 0 if select1 = select2, or
	 *         -1
	 */
	public static int compare(Selection select1, Selection select2) {
		int result;

		int paraIndex1 = select1.getParaIndex();
		int paraIndex2 = select2.getParaIndex();

		int selection1 = select1.getSelection();
		int selection2 = select2.getSelection();

		if (paraIndex1 == paraIndex2) {
			if (selection1 == selection2) {
				result = 0;
			} else if (selection1 > selection2) {
				result = 1;
			} else {
				result = -1;
			}
		} else if (paraIndex1 > paraIndex2) {
			result = 1;
		} else {
			result = -1;
		}

		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + mParaIndex;
		result = prime * result + mSelection;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Selection other = (Selection) obj;
		if (mParaIndex != other.mParaIndex)
			return false;
		if (mSelection != other.mSelection)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Selection [mParaIndex=" + mParaIndex + ", mSelection=" + mSelection + ", mSelectionX=" + mSelectionX
				+ ", mSelectionY=" + mSelectionY + ", mSelectionCharWidth=" + mSelectionCharWidth
				+ ", mSelectionCharHeight=" + mSelectionCharHeight + "]";
	}
}
