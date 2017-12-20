package com.sina.book.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.useraction.Constants;
import com.sina.book.useraction.UserActionManager;
import com.sina.book.util.MainThemeManager;

/**
 * 主页面主题设置
 * 
 * @author Tsimle
 * 
 */
public class MainThemeSettingActivity extends CustomTitleActivity implements OnClickListener {

	private View[] items;
	private View[] itemCheckFlags;

	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_main_theme_setting);
		initTitle();
		initView();
		// StorageUtil.saveBoolean("newfunc", false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.item0:
			checkTheme(0);
			break;
		case R.id.item1:
			checkTheme(1);
			break;
		case R.id.item2:
			checkTheme(2);
			break;
		case R.id.item3:
			checkTheme(3);
			break;
		 case R.id.item4:
		    checkTheme(4);
		 break;
		default:
			break;
		}
	}

	@Override
	public void onClickLeft() {
		this.finish();
	}

	private void initTitle() {
		TextView middleV = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middleV.setText(R.string.main_theme_txt);
		setTitleMiddle(middleV);

		View leftV = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleLeft(leftV);
	}

	private void initView() {
		items = new View[5];
		itemCheckFlags = new View[5];

		items[0] = findViewById(R.id.item0);
		itemCheckFlags[0] = findViewById(R.id.item0_check_img);
		items[0].setOnClickListener(this);

		items[1] = findViewById(R.id.item1);
		itemCheckFlags[1] = findViewById(R.id.item1_check_img);
		items[1].setOnClickListener(this);

		items[2] = findViewById(R.id.item2);
		itemCheckFlags[2] = findViewById(R.id.item2_check_img);
		items[2].setOnClickListener(this);

		items[3] = findViewById(R.id.item3);
		itemCheckFlags[3] = findViewById(R.id.item3_check_img);
		items[3].setOnClickListener(this);

		items[4] = findViewById(R.id.item4);
		itemCheckFlags[4] = findViewById(R.id.item4_check_img);
		items[4].setOnClickListener(this);

		checkTheme(MainThemeManager.getInstance().getCurTheme().type);
	}

	private void checkTheme(int type) {
		for (int i = 0; i < items.length; i++) {
			if (i == type) {
				itemCheckFlags[i].setVisibility(View.VISIBLE);
			} else {
				itemCheckFlags[i].setVisibility(View.GONE);
			}
		}
		MainThemeManager.getInstance().setCurTheme(MainThemeSettingActivity.this, type);

		UserActionManager.getInstance().recordEventValue(Constants.KEY_MAIN_THEME + type);
	}
}
