package org.geometerplus.fbreader.fbreader;

import org.geometerplus.zlibrary.core.options.ZLIntegerRangeOption;

import com.sina.book.reader.ReadStyleManager;
import com.sina.book.util.StorageUtil;

/**
 * 字体缩放事件
 * 
 * @author pin
 */
class ChangeFontSizeAction extends FBAction {
	private final int myDelta;

	ChangeFontSizeAction(FBReaderApp fbreader, int delta) {
		super(fbreader);
		myDelta = delta;
	}

	@Override
	protected void run(Object... params) {
		final ZLIntegerRangeOption option = Reader.ViewOptions
				.getTextStyleCollection().getBaseStyle().FontSizeOption;
		int size = option.getValue() + myDelta;

		if (myDelta < 0) {
			if (size <= ReadStyleManager.MIN_FONT_SIZE_SP) {
				size = (int) ReadStyleManager.MIN_FONT_SIZE_SP;
			}
		} else if (myDelta > 0) {
			if (size >= ReadStyleManager.MAX_FONT_SIZE_SP) {
				size = (int) ReadStyleManager.MAX_FONT_SIZE_SP;
			}
		}
		option.setValue(size);

		// FIXME：ouyang 保持和读书同步，保存一份
		StorageUtil.saveFloat(StorageUtil.KEY_FONT_SIZE, size);
		// StorageUtil.saveFloat(StorageUtil.KEY_FONT_SIZE, size -
		// ReadStyleManager.INS_FONT_SIZE_SP);
		Reader.clearTextCaches();
		Reader.getViewWidget().repaint();
	}
}
