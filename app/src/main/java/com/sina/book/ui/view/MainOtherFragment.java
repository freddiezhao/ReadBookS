package com.sina.book.ui.view;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.ui.SDCardActivity;

public class MainOtherFragment extends BaseFragment {

	private ListView mListView;
	private ItemAdapter mItemAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ArrayList<EnterItem> items = new ArrayList<MainOtherFragment.EnterItem>();
		// items.add(new EnterItem("包月", "点击开通包月，畅享千本好书！",
		// PaymentMonthActivity.class));
		// items.add(new EnterItem("已购买", "查看您已购买的书籍",
		// PurchasedActivity.class));
		// items.add(new EnterItem("微盘", "点击查看微盘收藏的好书", VDiskActivity.class));
		items.add(new EnterItem("本地", "随时随地在微博读书导入本地好书", SDCardActivity.class));
		mItemAdapter = new ItemAdapter(items);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_main_other, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		initView();
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onSelected() {

	}

	private void initView() {
		View root = getView();
		if (root == null) {
			throw new IllegalStateException("Content view not yet created");
		}
		mListView = (ListView) root.findViewById(R.id.content_lv);
		mListView.setAdapter(mItemAdapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				EnterItem item = (EnterItem) mItemAdapter.getItem(position);
				Intent intent = new Intent(getActivity(), item.enterActivityClass);
				intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
				// 包月传递Extra到包月列表
				if ("包月".equals(item.title)) {
					intent.putExtra("eventExtra", "书架");
				}
				startActivity(intent);
			}
		});
	}

	@SuppressWarnings("rawtypes")
	private class EnterItem {
		public String title;
		public String desc;
		public Class enterActivityClass;

		public EnterItem(String title, String desc, Class enterActivityClass) {
			super();
			this.title = title;
			this.desc = desc;
			this.enterActivityClass = enterActivityClass;
		}

	}

	private class ItemAdapter extends BaseAdapter {
		private ArrayList<EnterItem> items;

		public ItemAdapter(ArrayList<EnterItem> items) {
			this.items = items;
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View itemView;
			if (position == 0) {
				itemView = LayoutInflater.from(getActivity()).inflate(R.layout.vw_main_other_down_item, null);
			} else if (position == getCount() - 1) {
				itemView = LayoutInflater.from(getActivity()).inflate(R.layout.vw_main_other_up_item, null);
			} else {
				itemView = LayoutInflater.from(getActivity()).inflate(R.layout.vw_main_other_mid_item, null);
			}
			TextView titleTv = (TextView) itemView.findViewById(R.id.title);
			TextView descTv = (TextView) itemView.findViewById(R.id.desc);
			EnterItem item = (EnterItem) getItem(position);
			titleTv.setText(item.title);
			descTv.setText(item.desc);
			return itemView;
		}
	}
}
