package com.sina.book.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sina.book.R;
import com.sina.book.data.TopicItem;
import com.sina.book.image.ImageLoader;
import com.sina.book.ui.TopicActivity;
import com.sina.book.util.PixelUtil;

/**
 * 专题列表Adapter
 * 
 * @author MarkMjw
 * @date 13-12-12.
 */
public class TopicListAdapter extends ListAdapter<TopicItem> {
	private Context mContext;
	private LinearLayout.LayoutParams mParam;

	public TopicListAdapter(Context context) {
		mContext = context;

		mParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, PixelUtil.dp2px(92));
		mParam.topMargin = PixelUtil.dp2px(10.67f);
	}

	@Override
	protected List<TopicItem> createList() {
		return new ArrayList<TopicItem>();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (null == convertView || null == convertView.getTag()) {
			convertView = createView();
		}

		ViewHolder holder = (ViewHolder) convertView.getTag();

		final TopicItem item = mDataList.get(position);
		holder.textView.setText(item.getTitle());
		holder.imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				TopicActivity.launch(mContext, item.getTitle(), item.getTopicId(), TopicActivity.TYPE_OLD);
			}
		});
		ImageLoader.getInstance().load3(item.getImgUrl(), holder.imageView, ImageLoader.TYPE_BIG_PIC,
				ImageLoader.getDefaultHorizontalBannerPic());

		convertView.setLayoutParams(mParam);

		return convertView;
	}

	private View createView() {
		View view = LayoutInflater.from(mContext).inflate(R.layout.vw_topic_img_item, null);
		if (null != view) {
			ViewHolder holder = new ViewHolder();
			holder.imageView = (ImageView) view.findViewById(R.id.topic_image);
			holder.textView = (TextView) view.findViewById(R.id.topic_name);
			view.setTag(holder);
		}

		return view;
	}

	private class ViewHolder {
		private ImageView imageView;
		private TextView textView;
	}
}
