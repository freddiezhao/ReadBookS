package com.sina.book.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sina.book.R;
import com.sina.book.control.ITaskFinishListener;
import com.sina.book.control.RequestTask;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.BookDetailData;
import com.sina.book.data.ConstantData;
import com.sina.book.data.PurchasedBook;
import com.sina.book.data.util.CloudSyncUtil;
import com.sina.book.image.ImageLoader;
import com.sina.book.parser.BookDetailParser;
import com.sina.book.ui.widget.CustomProDialog;
import com.sina.book.util.PixelUtil;
import com.sina.book.util.ResourceUtil;
import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * 购买书籍列表适配器
 *
 * @author MarkMjw
 * @date 2012-12-25
 */
public class PurchasedBookAdapter extends ListAdapter<PurchasedBook> implements
        ITaskFinishListener {
    // private static final String TAG = "PurchasedBookAdapter";

    private ListView mListView;
    private Context mContext;
    private ViewHolder mHolder;

    private CustomProDialog mProgressDialog;
    private BitmapDrawable mDividerDrawable;

    private SparseBooleanArray mBooleanArray;

    public PurchasedBookAdapter(Context context, ListView listView) {
        mContext = context;
        mListView = listView;
        mBooleanArray = new SparseBooleanArray();

        Bitmap dotHBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.list_divide_dot);
        mDividerDrawable = new BitmapDrawable(mContext.getResources(), dotHBitmap);
        mDividerDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        mDividerDrawable.setDither(true);
    }

    @Override
    protected List<PurchasedBook> createList() {
        return new ArrayList<PurchasedBook>();
    }

    public void clearList() {
        if (mDataList != null) {
            mDataList.clear();
        }
    }

    public List<PurchasedBook> getDataList() {
        return mDataList;
    }

    private Book getBook(PurchasedBook pBook) {
        if (pBook != null) {
            Book b = new Book();
            b.setAuthor(pBook.getAuthor());
            b.setBookId(pBook.getBookId());
            b.setSid(pBook.getSid());
            b.setBookCate(pBook.getBookCate());
            b.setTitle(pBook.getTitle());
            b.getDownloadInfo().setImageUrl(pBook.getImageUrl());
            b.setIntro(pBook.getIntro());
            b.setBookSrc(pBook.getBookSrc());
            b.getBuyInfo().setBuyTime(pBook.getBuyTime());
            b.setBookCateId(pBook.getBookCateId());
            return b;
        }
        return null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || convertView.getTag() == null) {
            convertView = createView();
        }

        mHolder = (ViewHolder) convertView.getTag();
        PurchasedBook pBook = (PurchasedBook) getItem(position);
        // 转换成Book对象
        Book book = getBook(pBook);

        ImageLoader.getInstance().load(book.getDownloadInfo().getImageUrl(), mHolder.mBookCover,
                ImageLoader.TYPE_COMMON_BOOK_COVER, ImageLoader.getDefaultPic());

        mHolder.mBookTitle.setText(book.getTitle());
        mHolder.mBookAuthor.setText(mContext.getString(R.string.author) + book.getAuthor());
        if (book.getIntro() != null) {
            mHolder.mBookInfo.setText(book.getIntro().trim());
        } else {
            mHolder.mBookInfo.setText("No introduction.");
        }

        ClickListener listener = new ClickListener(mHolder, position, book, convertView);
        mHolder.mMenuBtn.setOnClickListener(listener);
        mHolder.mMenuDownBtn.setOnClickListener(listener);

        if (mBooleanArray.get(position, false)) {
            mHolder.mMenuBtn.setImageResource(R.drawable.menu_btn_up);
            mHolder.mMenuBtnLayout.setVisibility(View.VISIBLE);
        } else {
            mHolder.mMenuBtn.setImageResource(R.drawable.menu_btn_down);
            mHolder.mMenuBtnLayout.setVisibility(View.GONE);
        }

        Book managerBook = DownBookManager.getInstance().getBook(book);
        if (managerBook != null && !managerBook.isOnlineBook()) {
            mHolder.mMenuDownBtn.setEnabled(false);
            mHolder.mMenuDownBtn.setText(R.string.has_down);
        } else {
            mHolder.mMenuDownBtn.setEnabled(true);
            mHolder.mMenuDownBtn.setText(R.string.bookhome_down);
        }

        mHolder.mDivider.setImageDrawable(mDividerDrawable);

        return convertView;
    }

    protected View createView() {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.vw_purchased_list_item,
                null);

        ViewHolder holder = new ViewHolder();

        holder.mBookLayout = itemView.findViewById(R.id.item_content_layout);

        holder.mBookCover = (ImageView) holder.mBookLayout.findViewById(R.id.header_img);
        holder.mBookTitle = (TextView) holder.mBookLayout.findViewById(R.id.title);
        holder.mBookAuthor = (TextView) holder.mBookLayout.findViewById(R.id.author);
        holder.mBookInfo = (TextView) holder.mBookLayout.findViewById(R.id.book_info);

        holder.mMenuBtn = (ImageView) itemView.findViewById(R.id.item_menu_btn);
        holder.mMenuBtnLayout = (RelativeLayout) itemView.findViewById(R.id.item_menu_layout);
        holder.mMenuDownBtn = (TextView) itemView.findViewById(R.id.item_menu_btn_down);

        holder.mDivider = (ImageView) itemView.findViewById(R.id.item_divider);

        itemView.setTag(holder);
        return itemView;
    }

    protected class ViewHolder {
        public View mBookLayout;
        public ImageView mBookCover;
        public TextView mBookTitle;
        public TextView mBookAuthor;
        public TextView mBookInfo;

        public ImageView mMenuBtn;
        public RelativeLayout mMenuBtnLayout;
        public TextView mMenuDownBtn;

        public ImageView mDivider;
    }

    private class ClickListener implements View.OnClickListener {
        private ViewHolder mHolder;
        private int mPosition;
        private Book mBook;
        private View mView;

        public ClickListener(ViewHolder holder, int position, Book book, View view) {
            mHolder = holder;
            mPosition = position;
            mBook = book;
            mView = view;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.item_menu_btn:
                    mBooleanArray.clear();
                    if (!mHolder.mMenuBtnLayout.isShown()) {
                        mBooleanArray.put(mPosition, true);
                        notifyDataSetChanged();

                        // 列表菜单栏的高度
                        int menuHeight = (int) ResourceUtil.getDimens(R.dimen
                                .bookhome_item_menu_height);
                        if (mListView.getHeight() - mView.getBottom() < menuHeight) {
                            int itemMinHeight = v.getHeight();
                            int height = mListView.getHeight() - itemMinHeight - menuHeight;

                            int curPosition = mPosition + mListView.getHeaderViewsCount() -
                                    height / itemMinHeight;

                            if (curPosition < mDataList.size() && curPosition >= 0) {
                                mListView.setSelectionFromTop(curPosition,
                                        height % itemMinHeight - PixelUtil.dp2px(3));
                            }
                        }
                    } else {
                        notifyDataSetChanged();
                    }

                    break;

                case R.id.item_menu_btn_down:
                    mBooleanArray.clear();
                    notifyDataSetChanged();

                    // 非在线书籍下载
                    if (!DownBookManager.getInstance().hasBook(mBook)) {
                        showProgressDialog(R.string.downloading_text);
                        reqBookInfo(mBook);
                    } else {
                        DownBookManager.getInstance().downBook(mBook);
                    }

                    break;

                default:
                    break;
            }
        }
    }

    /**
     * 显示进度条.
     *
     * @param resId the res id
     */
    private void showProgressDialog(int resId) {
        if (null == mProgressDialog) {
            mProgressDialog = new CustomProDialog(mContext);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setCanceledOnTouchOutside(false);
        }

        mProgressDialog.show(resId);
    }

    /**
     * 隐藏进度条.
     */
    private void dismissProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Req book info.
     *
     * @param book the book
     */
    private void reqBookInfo(Book book) {
        showProgressDialog(R.string.downloading_text);
        String reqUrl = String.format(ConstantData.URL_BOOK_INFO, book.getBookId(),
                book.getSid(), book.getBookSrc());
        reqUrl = ConstantData.addLoginInfoToUrl(reqUrl);
        RequestTask reqTask = new RequestTask(new BookDetailParser());
        reqTask.setTaskFinishListener(this);
        TaskParams params = new TaskParams();
        params.put(RequestTask.PARAM_URL, reqUrl);
        params.put(RequestTask.PARAM_HTTP_METHOD, RequestTask.HTTP_GET);
        reqTask.execute(params);
    }

    @Override
    public void onTaskFinished(TaskResult taskResult) {
        if (taskResult != null && taskResult.stateCode == HttpStatus.SC_OK) {
            if (taskResult.retObj instanceof BookDetailData) {
                BookDetailData data = (BookDetailData) taskResult.retObj;
                DownBookManager.getInstance().downBook(data.getBook(), true);
                CloudSyncUtil.getInstance().add2Cloud(mContext, data.getBook());
                notifyDataSetChanged();

                dismissProgressDialog();
                return;
            }
        }

        dismissProgressDialog();
        Toast.makeText(mContext, R.string.bookhome_net_error, Toast.LENGTH_SHORT).show();
    }
}
