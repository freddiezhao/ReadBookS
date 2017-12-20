package com.sina.book.ui;

import org.geometerplus.fbreader.book.Author;
import org.geometerplus.fbreader.fbreader.FBReaderApp;
import org.geometerplus.zlibrary.core.application.ZLApplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.ui.view.EpubChapterFragment;

/**
 * Epub书籍，从书摘点击目录进入的目录页
 * 
 * @author chenjl
 * 
 */
public class EpubChapterActivity extends FragmentActivity {

	private ImageView mGoBtn;
	private TextView mBookName;
	private TextView mBookAuthor;
	private ImageView mDivider;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.act_epub_chapter_layout);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.lv_chapter, new EpubChapterFragment()).commit();
		}

		initView();
		initTitle();
	}

	private void initView() {
		Bitmap dotHBitmap = BitmapFactory.decodeResource(getResources(),
				R.drawable.list_divide_dot);
		BitmapDrawable dotHDrawable = new BitmapDrawable(getResources(),
				dotHBitmap);
		dotHDrawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		dotHDrawable.setDither(true);

		mGoBtn = (ImageView) findViewById(R.id.go_btn);
		mBookName = (TextView) findViewById(R.id.book_name);
		mBookAuthor = (TextView) findViewById(R.id.book_author);

		mDivider = (ImageView) findViewById(R.id.book_tag_divider);
		mDivider.setBackgroundDrawable(dotHDrawable);
	}

	private void initTitle() {
		// 获取全局的变量
		FBReaderApp fbreader = (FBReaderApp) ZLApplication.Instance();
		org.geometerplus.fbreader.book.Book book = fbreader.Model.Book;
		if (book != null) {
			// 书名
			mBookName.setText(book.getTitle());
			// 作者
			if (!book.authors().isEmpty()) {
				StringBuilder author = new StringBuilder();
				boolean first = true;
				for (Author a : book.authors()) {
					author.append(first ? "" : ", ");
					author.append(a.DisplayName);
					first = false;
				}
				mBookAuthor.setText(author.toString());
			} else {
				mBookAuthor.setText("未知");
			}
		}

		mGoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

}
