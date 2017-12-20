package com.sina.book.ui.view;

import org.geometerplus.android.fbreader.OrientationUtil;
import org.geometerplus.android.fbreader.ZLTreeAdapter;
import org.geometerplus.android.util.ViewUtil;
import org.geometerplus.fbreader.bookmodel.TOCTree;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.tree.ZLTree;
import org.geometerplus.zlibrary.text.view.ZLTextWordCursor;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.reader.ReadStyleManager;
import com.sina.book.ui.BookTagActivity;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;

/**
 * 展示Epub的目录Fragment(由阅读页点击工具栏->目录进入)
 * 
 * @author chenjl
 * 
 */
public class EpubChapterFragment extends BaseFragment {

	/** 当前章节标志图的高度 */
	private final int CUR_MARK_HEIGHT = PixelUtil.dp2px(42.67f);
	/** 当前章节标志图的宽度 */
	private final int CUR_MARK_WIDTH = PixelUtil.dp2px(2.0f);

	// 数据
	private TOCAdapter myAdapter;
	private ZLTree<?> mySelectedItem;
	// 视图
	private ListView mListView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_epub_chapter, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initView();
		initData();
		super.onViewCreated(view, savedInstanceState);
	}

	boolean isHtmlChapter = false;
	private void initView() {
		Activity activity = getActivity();
		if(activity instanceof BookTagActivity){
			BookTagActivity bookActivity = (BookTagActivity)activity;
			if(bookActivity.mBook != null && bookActivity.mBook.isHtmlRead()){
				isHtmlChapter = true;
			}
		}
		
		View root = getView();
		if (root == null) {
			throw new IllegalStateException("Content View not yet created.");
		}

		mListView = (ListView) ViewUtil.findView(root, R.id.lv_chapter);
		
		if(isHtmlChapter){
			Drawable divider = getResources().getDrawable(R.drawable.divider_line);
			mListView.setDivider(divider);
		}else{
			ReadStyleManager styleManager = ReadStyleManager
					.getInstance(getActivity());
			
//		 int readBgColor = styleManager.getColorFromIdentifier(getActivity(),
//		 R.color.book_tag_mark_bg);
//		 mListView.setBackgroundColor(readBgColor);
			
			Drawable divider = styleManager.getDrawableFromIdentifier(
					getActivity(), R.drawable.divider_line);
			mListView.setDivider(divider);
		}
	}

	private void initData() {
		final FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		// 目录树结构对象
		final TOCTree root = fbreader.Model.TOCTree;
		myAdapter = new TOCAdapter(root, true);
		final ZLTextWordCursor cursor = fbreader.BookTextView.getStartCursor();
		int index = cursor.getParagraphIndex();
		if (cursor.isEndOfParagraph()) {
			++index;
		}
		TOCTree treeToSelect = fbreader.getCurrentTOCElement();
		myAdapter.selectItem(treeToSelect);
		mySelectedItem = treeToSelect;
	}

	private ListView getListView() {
		return mListView;
	}

	@Override
	public void onSelected() {

	}

	@Override
	public void onStart() {
		super.onStart();
		OrientationUtil
				.setOrientation(getActivity(), getActivity().getIntent());
	}

	private static final int PROCESS_TREE_ITEM_ID = 0;
	private static final int READ_BOOK_ITEM_ID = 1;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final int position = ((AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo()).position;
		final TOCTree tree = (TOCTree) myAdapter.getItem(position);
		switch (item.getItemId()) {
		case PROCESS_TREE_ITEM_ID:
			myAdapter.runTreeItem(tree);
			return true;
		case READ_BOOK_ITEM_ID:
			myAdapter.openBookText(tree);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	public final class TOCAdapter extends ZLTreeAdapter {
		private boolean isNeedApplyThemeMode = false;

		TOCAdapter(TOCTree root) {
			super(getListView(), root);
		}

		TOCAdapter(TOCTree root, boolean isNeedApplyThemeMode) {
			super(getListView(), root);
			this.isNeedApplyThemeMode = isNeedApplyThemeMode;
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View view,
				ContextMenu.ContextMenuInfo menuInfo) {
			final int position = ((AdapterView.AdapterContextMenuInfo) menuInfo).position;
			final TOCTree tree = (TOCTree) getItem(position);
			if (tree.hasChildren()) {
				menu.setHeaderTitle(tree.getText());
				final ZLResource resource = ZLResource.resource("tocView");
				menu.add(
						0,
						PROCESS_TREE_ITEM_ID,
						0,
						resource.getResource(
								isOpen(tree) ? "collapseTree" : "expandTree")
								.getValue());
				menu.add(0, READ_BOOK_ITEM_ID, 0,
						resource.getResource("readText").getValue());
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Context context = parent.getContext();
			final View view = (convertView != null) ? convertView
					: LayoutInflater.from(context).inflate(
							R.layout.vw_epub_chapter_item, parent, false);
			TextView groupOrChapterText = ViewUtil.findTextView(view,
					R.id.chapter_group);
			ImageView groupIndicatorIcon = ViewUtil.findImageView(view,
					R.id.chapter_group_icon);

			final TOCTree tree = (TOCTree) getItem(position);

			
			if (!isNeedApplyThemeMode) {
				// 摘要页内的目录
				view.setBackgroundDrawable(context.getResources().getDrawable(
						R.drawable.mark_list_item_bg));
				groupOrChapterText.setTextColor(context.getResources()
						.getColor(getTOCTreeTextColor(tree)));
			} else {
				// 阅读页内的目录
				if(isHtmlChapter){
					view.setBackgroundResource(R.drawable.mark_list_item_bg);
					groupOrChapterText.setTextColor(getResources().getColor(getTOCTreeTextColor(tree)));
				}else{
					ReadStyleManager readStyleManager = ReadStyleManager
							.getInstance(context);
					
					if (ReadStyleManager.READ_MODE_NIGHT == readStyleManager
							.getReadMode()) {
						view.setBackgroundDrawable(context.getResources()
								.getDrawable(R.drawable.mark_list_item_bg_night));
					} else {
						view.setBackgroundDrawable(context.getResources()
								.getDrawable(R.drawable.mark_list_item_bg));
					}
					groupOrChapterText.setTextColor(readStyleManager
							.getColorFromIdentifier(context,
									getTOCTreeTextColor(tree)));
				}
			}

			// 当前阅读章节项
			if (tree == mySelectedItem) {
				// 设置选中背景
				groupOrChapterText.setTextColor(ResourceUtil
						.getColor(R.color.current_chapter_color));
				Drawable drawable = context.getResources().getDrawable(
						R.drawable.current_chapter_mark_normal);
				drawable.setBounds(0, 0, CUR_MARK_WIDTH, CUR_MARK_HEIGHT);
				groupOrChapterText.setCompoundDrawables(drawable, null, null,
						null);

				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) groupOrChapterText
						.getLayoutParams();
				params.setMargins(0, 0, 0, 0);
				groupOrChapterText.setLayoutParams(params);
			} else {
				// 还原选中背景
				groupOrChapterText.setCompoundDrawables(null, null, null, null);
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) groupOrChapterText
						.getLayoutParams();
				params.setMargins(18, 0, 0, 0);
				groupOrChapterText.setLayoutParams(params);
			}

			// 有卷
			if (tree.hasChildren()) {
				// 卷处于打开状态
				if (isOpen(tree)) {
					groupIndicatorIcon
							.setImageResource(R.drawable.expand_y_normal);
				} else {
					// 关闭状态
					groupIndicatorIcon
							.setImageResource(R.drawable.expand_n_normal);
				}
			} else {
				// 无卷
				groupIndicatorIcon
						.setImageResource(R.drawable.ic_list_group_empty);
			}
			groupIndicatorIcon.setPadding(25 * (tree.Level - 1),
					groupIndicatorIcon.getPaddingTop(), 0,
					groupIndicatorIcon.getPaddingBottom());

			// DEBUG CJL 为了查看章节，在前面加上索引
			groupOrChapterText.setText(tree.getText());// "(" + (position + 1) +
														// ")"
			return view;
		}

		private int getTOCTreeTextColor(TOCTree tree) {
			if (tree != null && tree.hasChildren()) {
				return R.color.book_chapter_title_font_color;
			}
			return R.color.book_chapter_info_font_color;
		}

		/**
		 * 打开某个章节
		 * 
		 * @param tree
		 */
		void openBookText(TOCTree tree) {
			final TOCTree.Reference reference = tree.getReference();
			if (reference != null) {
				getActivity().finish();
				final FBReaderApp fbreader = (FBReaderApp) ZLApplication
						.Instance();
				fbreader.addInvisibleBookmark();
				fbreader.BookTextView.gotoPosition(reference.ParagraphIndex, 0,
						0);
				fbreader.showBookTextView();
				fbreader.storePosition();
			}
		}

		@Override
		protected boolean runTreeItem(ZLTree<?> tree) {
			// 先通过父类的方法判断是否点击了卷，是展开还是收缩
			if (super.runTreeItem(tree)) {
				return true;
			}
			openBookText((TOCTree) tree);
			return true;
		}
	}

}
