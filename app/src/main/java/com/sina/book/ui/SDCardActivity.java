package com.sina.book.ui;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.geometerplus.android.fbreader.FBReader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sina.book.R;
import com.sina.book.control.GenericTask;
import com.sina.book.control.ICancelable;
import com.sina.book.control.TaskParams;
import com.sina.book.control.TaskResult;
import com.sina.book.control.download.DownBookJob;
import com.sina.book.control.download.DownBookManager;
import com.sina.book.data.Book;
import com.sina.book.data.Chapter;
import com.sina.book.db.DBService;
import com.sina.book.exception.UnparseEpubFileException;
import com.sina.book.reader.EpubFileHandler;
import com.sina.book.ui.adapter.CategoryAdapter;
import com.sina.book.util.DialogUtils;
import com.sina.book.util.StorageUtil;
import com.sina.book.util.Util;

/**
 * 本地导入界面.
 */
public class SDCardActivity extends CustomTitleActivity {

	/** 扫描文件大小限制，暂定位5KB. */
	private static final int SCAN_FILE_SIZE_LIMIT = 5 * 1024;
	
	/** sd卡浏览视图状态. */
	private static final int VIEW_SDCARD = 0;

	/** sd卡扫描视图状态. */
	private static final int VIEW_SCAN = 1;

	/** 文件列表. */
	private ListView mFileListView = null;

	/** 文件名集合. */
	private List<String> mFileName = null;

	/** 文件路径集合. */
	private List<File> mFiles = null;

	/** sd卡路径. */
	private String mSDCard = Environment.getExternalStorageDirectory().toString();

	/** sd卡父路径. */
	private String mRootPath = Environment.getExternalStorageDirectory().getParent();

	/** 当前文件夹路径. */
	private TextView mPathTextView;

	/** 扫描按钮. */
	private TextView mScanButton;

	/** 当前目录路径信息. */
	private String mCurrentFilePath = ".";

	/** 1代表SD卡父目录为根目录，2代表SD卡为根目录。便于产品改需求. */
	private int mRootPathType = 1;

	/** 视图状态. */
	private int mState;

	/** txt文件的文件适配器. */
	private ScanFileAdapter mTxtScanFileAdapter;

	/** epub文件的文件适配器. */
	private ScanFileAdapter mEpubScanFileAdapter;

	/** txt文件名集合. */
	private List<String> mTxtFileNames = new ArrayList<String>();

	/** txt文件集合. */
	private List<File> mTxtFiles = new ArrayList<File>();

	/** epub文件名集合. */
	private List<String> mEpubFileNames = new ArrayList<String>();

	/** epub文件集合. */
	private List<File> mEpubFiles = new ArrayList<File>();

	private FileAdapter mFileAdapter;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sina.book.ui.CustomTitleActivity#init(android.os.Bundle)
	 */
	@Override
	protected void init(Bundle savedInstanceState) {
		setContentView(R.layout.act_sdcard);
		initView();
		initTitle();
		resgisterListener();
		initFileListInfo(mSDCard);
	}

	/**
	 * 初始化标题.
	 */
	private void initTitle() {
		TextView middle = (TextView) LayoutInflater.from(this).inflate(R.layout.vw_title_textview, null);
		middle.setText(R.string.sdcard_title);
		View left = LayoutInflater.from(this).inflate(R.layout.vw_generic_title_back, null);
		setTitleMiddle(middle);
		setTitleLeft(left);
	}

	/**
	 * 初始化视图.
	 */
	private void initView() {
		mFileListView = (ListView) findViewById(R.id.file_list);
		mEpubScanFileAdapter = new ScanFileAdapter(SDCardActivity.this, mEpubFileNames, mEpubFiles);
		mTxtScanFileAdapter = new ScanFileAdapter(SDCardActivity.this, mTxtFileNames, mTxtFiles);
		mPathTextView = (TextView) findViewById(R.id.path_text);
		mScanButton = (TextView) findViewById(R.id.scan_btn);
	}

	/**
	 * Resgister listener.
	 */
	private void resgisterListener() {
		mFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (Util.isFastDoubleClick(2000)) {
					return;
				}

				if (mState == VIEW_SDCARD) {
					File mFile = mFiles.get(position);
					clickFileItem(mFile);
				} else if (mState == VIEW_SCAN) {
					File mFile = null;
					if (position > 0 && position < mEpubFileNames.size() + 1) {
						// 由于epub有个title所以-1
						mFile = mEpubFiles.get(position - 1);
						clickFileItem(mFile);
					} else if (position > mEpubFileNames.size() + 1) {
						// 由于txt的item在epublist下，并且有两个title，故下面这样处理
						mFile = mTxtFiles.get(position - mEpubFileNames.size() - 2);
						clickFileItem(mFile);
					}
				}
			}
		});

		mScanButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 进入扫描视图状态
				mState = VIEW_SCAN;
				mScanButton.setVisibility(View.INVISIBLE);
				mCategoryAdapter.clearCategorys();
				mTxtFileNames.clear();
				mTxtFiles.clear();
				mEpubFileNames.clear();
				mEpubFiles.clear();
				mCategoryAdapter.addCategory(String.format(getResources().getString(R.string.epub_number), 0),
						mEpubScanFileAdapter);
				mCategoryAdapter.addCategory(String.format(getResources().getString(R.string.txt_number), 0),
						mTxtScanFileAdapter);
				mFileListView.setAdapter(mCategoryAdapter);
				scanFile();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sina.book.ui.CustomTitleActivity#onResume()
	 */
	public void onResume() {
		super.onResume();
		if (mFileAdapter != null) {
			mFileAdapter.notifyDataSetChanged();
		}
		if (mCategoryAdapter != null) {
			mCategoryAdapter.notifyDataSetChanged();
		}
		if (mTxtScanFileAdapter != null) {
			mTxtScanFileAdapter.notifyDataSetChanged();
		}
		if (mEpubScanFileAdapter != null) {
			mEpubScanFileAdapter.notifyDataSetChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sina.book.ui.CustomTitleActivity#onClickLeft()
	 */
	public void onClickLeft() {
		finish();
	}

	/**
	 * Click file item.
	 * 
	 * @param file
	 *            the file
	 */
	private void clickFileItem(File file) {
		if (file == null) {
			return;
		}
		if (file.exists()) {
			if (file.canRead()) {// 如果该文件是可读的，我们进去查看文件
				if (file.isDirectory()) {// 如果是文件夹，则直接进入该文件夹，查看文件目录
					initFileListInfo(file.getAbsolutePath());
				} else {// 如果是文件，则用相应的打开方式打开
					openFile(file);
				}
			} else {// 如果该文件不可读，我们给出提示不能访问，防止用户操作系统文件造成系统崩溃等
				Toast.makeText(SDCardActivity.this, getString(R.string.do_not_have_access), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(SDCardActivity.this, getString(R.string.the_file_has_been_deleted), Toast.LENGTH_SHORT)
					.show();
		}
	}

	/**
	 * 扫描文件.
	 */
	private void scanFile() {
		// 启动异步扫描任务
		final GenericTask scanTask = new GenericTask() {

			@Override
			protected TaskResult doInBackground(TaskParams... params) {
				refreshScanResults(mCurrentFilePath);
				return null;
			}

			private void refreshScanResults(final String nowDir) {
				final File file = new File(nowDir);
				final File[] txtOrEpubOrDirFiles = listFiles(file, new TXTOrEPUBOrDIRLengthLimitSelector());

				for (final File f : txtOrEpubOrDirFiles) {
					if (!f.canRead()) {
						continue;
					}
					if (f.isDirectory()) {
						// 过滤软件自己的文件夹
						File homePath = new File(StorageUtil.EXTERNAL_STORAGE + StorageUtil.DIR_HOME);
						if (!isCancelled() && !f.equals(homePath)) {
							refreshScanResults(f.getAbsolutePath());
						}
					} else {
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								if (f.getName().toLowerCase(Locale.CHINA).endsWith(".epub")) {
									mEpubFileNames.add(f.getName());
									mEpubFiles.add(f);
								} else if (f.getName().toLowerCase(Locale.CHINA).endsWith(".txt")) {
									mTxtFileNames.add(f.getName());
									mTxtFiles.add(f);
								}
								// 根据大小对集合进行排序，大的排前面
								Collections.sort(mEpubFiles, new Comparator<File>() {
									@Override
									public int compare(File o1, File o2) {
										if (o1.length() > o2.length()) {
											return -1;
										} else if (o1.length() < o2.length()) {
											return 1;
										} else {
											return 0;
										}
									}
								});
								// 根据大小对集合进行排序，大的排前面
								Collections.sort(mTxtFiles, new Comparator<File>() {
									@Override
									public int compare(File o1, File o2) {
										if (o1.length() > o2.length()) {
											return -1;
										} else if (o1.length() < o2.length()) {
											return 1;
										} else {
											return 0;
										}
									}
								});
								mCategoryAdapter.updateTitleCategory(0, String.format(
										getResources().getString(R.string.epub_number), mEpubFiles.size()));
								mCategoryAdapter.updateTitleCategory(1,
										String.format(getResources().getString(R.string.txt_number), mTxtFiles.size()));
								mCategoryAdapter.notifyDataSetChanged();
								DialogUtils.updateProgressDialog(String.format(getString(R.string.scan_result),
										mEpubFiles.size(), mTxtFiles.size()));
							}
						});
					}
				}

			}

			protected void onPostExecute(TaskResult result) {
				DialogUtils.dismissProgressDialog();
			}
		};
		scanTask.execute();
		// 显示进度对话框，设置为可以取消
		DialogUtils.showProgressDialog(SDCardActivity.this,
				String.format(getResources().getString(R.string.scan_result), 0, 0), true, true,
				new OnCancelListener() {

					@Override
					public void onCancel(DialogInterface dialog) {
						if (scanTask != null) {
							scanTask.cancel(true);
						}
					}

				}, null);
	}

	/**
	 * 打开文件.
	 * 
	 * @param file
	 *            the file
	 */
	private void openFile(File file) {
		if (file.isFile()) {
			if (file.getName().toLowerCase(Locale.CHINA).endsWith(".txt")) {
				openTxt(file);
			} else if (file.getName().toLowerCase(Locale.CHINA).endsWith(".epub")) {
				// 打开epub文件
				openEPUB(file);
			} else {
				// 其实这里是进不来的，还是做下处理吧
				Intent intent = new Intent();
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setAction(android.content.Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(file), getMIMEType(file));
				startActivity(intent);
			}
		}
	}

	/**
	 * 获得MIME类型的方法.
	 * 
	 * @param file
	 *            the file
	 * @return the mIME type
	 */
	private String getMIMEType(File file) {
		String type = "";
		String fileName = file.getName();
		String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
				.toLowerCase(Locale.CHINA);// 取出文件后缀名并转成小写
		if (fileEnds.equals("m4a") || fileEnds.equals("mp3") || fileEnds.equals("mid") || fileEnds.equals("xmf")
				|| fileEnds.equals("ogg") || fileEnds.equals("wav")) {
			type = "audio/*";// 系统将列出所有可能打开音频文件的程序选择器
		} else if (fileEnds.equals("3gp") || fileEnds.equals("mp4")) {
			type = "video/*";// 系统将列出所有可能打开视频文件的程序选择器
		} else if (fileEnds.equals("jpg") || fileEnds.equals("gif") || fileEnds.equals("png")
				|| fileEnds.equals("jpeg") || fileEnds.equals("bmp")) {
			type = "image/*";// 系统将列出所有可能打开图片文件的程序选择器
		} else {
			type = "*/*"; // 系统将列出所有可能打开该文件的程序选择器
		}
		return type;
	}

	/**
	 * 根据文件生成book对象.
	 * 
	 * @param file
	 *            the file
	 * @return the book
	 */
	private Book createBookFromFile(File file) {
		final Book book = new Book();
		// 注意，这里没有设置book的FilePath是因为epub文件需要转换为txt，所以只设置了文件的原始路径，
		// txt可以再创建txt文件book对象时进行设置，而epub则在转换epub文件时进行设置
		book.getDownloadInfo().setOriginalFilePath(file.getAbsolutePath());
		book.getDownloadInfo().setFileSize(file.length());
		book.getDownloadInfo().setDownLoadState(DownBookJob.STATE_FINISHED);
		book.getDownloadInfo().setImageUrl(Book.SDCARD_PATH_IMG + file.getAbsolutePath());
		int dotIndex = file.getName().lastIndexOf(".");
		StringBuilder sb = new StringBuilder(file.getName().substring(0, dotIndex));
		book.setTitle(sb.toString());
		book.getDownloadInfo().setProgress(1.0);
		book.getBuyInfo().setPayType(Book.BOOK_TYPE_FREE);
		book.getDownloadInfo().setLocationType(Book.BOOK_SDCARD);
		book.setAuthor(getString(R.string.unkonw_author));
		return book;
	}

	/**
	 * 生成txt文件的book对象.
	 * 
	 * @param txtFile
	 *            the txt file
	 * @return the book
	 */
	private Book createTxtBook(final File txtFile) {
		final Book book = createBookFromFile(txtFile);
		book.getDownloadInfo().setFilePath(txtFile.getAbsolutePath());
		// 本地txt书籍由于没有章节信息，章节总数设为1
		book.setNum(1);
		Chapter chapter = new Chapter();
		chapter.setChapterId(1);
		chapter.setStartPos(0);
		chapter.setLength(txtFile.length());
		int dotIndex = txtFile.getName().lastIndexOf(".");
		StringBuilder sb = new StringBuilder(txtFile.getName().substring(0, dotIndex));
		chapter.setTitle(sb.toString());
		ArrayList<Chapter> chapters = new ArrayList<Chapter>();
		chapters.add(chapter);
		book.setChapters(chapters);
		return book;
	}

	
	/**
	 * 打开txt书籍.
	 * 
	 * @param txtFile
	 *            the txt file
	 */
	private void openTxt(final File txtFile) {
		ReadActivity.setChapterReadEntrance("导入本地书籍");
		Book book = createTxtBook(txtFile);
		if (DownBookManager.getInstance().hasBook(book)) {
			book = DownBookManager.getInstance().getBook(book);
			ReadActivity.launch(SDCardActivity.this, book, false, false);
		} else {
			ReadActivity.launch(SDCardActivity.this, book, false, false);
		}
	}

	/**
	 * 生成epub文件的book对象.
	 * 
	 * @param epubFile
	 *            the epub file
	 * @return the book
	 */
	private Book createEpubBook(final File epubFile, ICancelable task) {
		Book book = createBookFromFile(epubFile);
		// 如果书架中有这本书，就知道打开，不用解析
		if (DownBookManager.getInstance().hasBook(book)) {
			book = DownBookManager.getInstance().getBook(book);
		} else {
			// 转换完成后book的文件路径会到sina/reader/books中
			//FIXME:
			
			try {
				EpubFileHandler.convertEpub2Dat(book, task);
			} catch (UnparseEpubFileException e) {
				book = null;
			}
		}
		return book;
	}
	
	/**
	 * 打开epub书籍.
	 * 
	 * @param epubFile
	 *            the epub file
	 */
	private void openEPUB(final File epubFile) {
		//FIXME:ouyang epub资源更改为打开FBReader
		
		boolean hasPercent = false;
		Book book = createBookFromFile(epubFile);
		if (DownBookManager.getInstance().hasBook(book)) {
			book = DownBookManager.getInstance().getBook(book);
			float precent = book.getReadInfo().getLastReadPercent();
			hasPercent = precent > 0.0f? true:false;
		}else{
			hasPercent = false;
		}
		
		FBReader.openBookActivity(SDCardActivity.this,epubFile.getAbsolutePath(), hasPercent);
		
//		final GenericTask openEpubTask = new GenericTask() {
//			Book book = createBookFromFile(epubFile);
//			protected void onPreExecute() {
//				DialogUtils.showProgressDialog(SDCardActivity.this, R.string.opening_file, true,
//						new OnCancelListener() {
//
//							@Override
//							public void onCancel(DialogInterface dialog) {
//								// 取消异步任务
//								cancel(true);
//							}
//						}, null);
//			}
//
//			@Override
//			protected TaskResult doInBackground(TaskParams... params) {
//				// 如果书架中有这本书，就直接打开，不用解析
//				if (DownBookManager.getInstance().hasBook(book)) {
//					book = DownBookManager.getInstance().getBook(book);
//				} else {
//					try {
//						EpubFileHandler.convertEpub2Dat(book, this);
//					} catch (UnparseEpubFileException e) {
//						book = null;
//					}
//				}
//				return null;
//			}
//
//			protected void onPostExecute(TaskResult result) {
//				DialogUtils.dismissProgressDialog();
//				if (book != null) {
//					ReadActivity.setChapterReadEntrance("导入本地书籍");
//					ReadActivity.launch(SDCardActivity.this, book, false, false);
//				} else {
//					shortToast(R.string.can_not_parse);
//				}
//			}
//		};
//		openEpubTask.execute();
	}

	/**
	 * 导入文件到书架.
	 * 
	 * @param adapter
	 *            the adapter
	 * @param fileEnds
	 *            the file ends
	 * @param file
	 *            the file
	 */
	private void joinFileToShelves(final BaseAdapter adapter, final String fileEnds, final File file) {
		final GenericTask joinFileTask = new GenericTask() {
			Book book = null;

			protected void onPreExecute() {
				DialogUtils.showProgressDialog(SDCardActivity.this, R.string.waiting_join_file, true,
						new OnCancelListener() {

							@Override
							public void onCancel(DialogInterface dialog) {
								// 取消异步任务
								cancel(true);
							}
						}, null);
			}

			protected TaskResult doInBackground(TaskParams... params) {
				if (fileEnds.equals("txt")) {
					book = createTxtBook(file);
				} else if (fileEnds.equals("epub")) {
					book = createEpubBook(file, this);
				}
				return null;
			}
			
			protected void onPostExecute(TaskResult result) {
				DialogUtils.dismissProgressDialog();
				if (book != null) {
					if (Util.isNullOrEmpty(book.getAuthor())) {
						book.setAuthor(getString(R.string.unkonw_author));
					}

					book.getDownloadInfo().setDownloadTime(new Date().getTime());
					DownBookManager.getInstance().addBookToShelves(book);
					DBService.saveBook(book);
					DBService.updateAllChapter(book, book.getChapters());
					shortToast(R.string.add_shelves_down);
					adapter.notifyDataSetChanged();
					mCategoryAdapter.notifyDataSetChanged();
				} else {
					shortToast(R.string.can_not_parse);
				}
			}
		};
		joinFileTask.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#dispatchKeyEvent(android.view.KeyEvent)
	 */
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
			if (mState == VIEW_SCAN) {
				// 在扫描视图时点击返回键，从扫描视图切换回sd卡视图
				mState = VIEW_SDCARD;
				mScanButton.setVisibility(View.VISIBLE);
				initFileListInfo(mCurrentFilePath);
			} else if (mState == VIEW_SDCARD) {
				// 在sd视图时，点击返回键返回上层目录，否则退出本地导入界面
				File file = new File(mCurrentFilePath);
				if (file.getParent() != null && !file.getAbsolutePath().equals(mRootPath)) {
					initFileListInfo(file.getParent());
				} else {
					finish();
				}
			}
			return true;
		}
		return super.dispatchKeyEvent(event);
	}

	/**
	 * 根据给定的一个文件夹路径字符串遍历出这个文 件夹中包含的文件名称并配置到ListView列表中.
	 * 
	 * @param filePath
	 *            the file path
	 */
	private void initFileListInfo(String filePath) {
		mCurrentFilePath = filePath;
		mPathTextView.setText(filePath);
		mFileName = new ArrayList<String>();
		mFiles = new ArrayList<File>();
		File mFile = new File(filePath);
		List<File> files = Arrays.asList(listFiles(mFile, new TXTOrEPUBOrDIRSelector()));
		// 为文件排序，文件夹优先，根据字母顺序优先，且无视大小写
		Collections.sort(files, new Comparator<File>() {
			@Override
			public int compare(File o1, File o2) {
				if (o1.isDirectory() && o2.isFile())
					return -1;
				if (o1.isFile() && o2.isDirectory())
					return 1;
				return o1.getName().toLowerCase(Locale.CHINA).compareTo(o2.getName().toLowerCase(Locale.CHINA));
			}
		});
		if (mRootPathType == 1 && !mCurrentFilePath.equals(mRootPath)) {
			initAddBackUp(filePath, mRootPath);
		} else if (mRootPathType == 2 && !mCurrentFilePath.equals(mSDCard)) {
			initAddBackUp(filePath, mSDCard);
		}

		/* 将所有文件信息添加到集合中 */
		for (File mCurrentFile : files) {
			mFileName.add(mCurrentFile.getName());
			mFiles.add(mCurrentFile);
		}

		mFileAdapter = new FileAdapter(SDCardActivity.this, mFileName, mFiles);
		/* 适配数据 */
		mFileListView.setAdapter(mFileAdapter);
	}

	/**
	 * Inits the add back up.
	 * 
	 * @param filePath
	 *            the file path
	 * @param phone_sdcard
	 *            the phone_sdcard
	 */
	private void initAddBackUp(String filePath, String phone_sdcard) {
		if (!filePath.equals(phone_sdcard)) {
			mFileName.add("BackToParent");
			mFiles.add(new File(filePath).getParentFile());// 回到当前目录的父目录即回到上级
		}
	}

	/**
	 * 不会返回null的listFile方法
	 * 
	 * @param file
	 * @param fileFilter
	 * @return
	 */
	private File[] listFiles(File file, FileFilter fileFilter) {
		File[] listFiles = file.listFiles(fileFilter);
		if (listFiles != null) {
			return listFiles;
		}
		return new File[0];
	}

	/** 扫描时epub和txt用到的分类适配器. */
	private CategoryAdapter mCategoryAdapter = new CategoryAdapter() {
		@Override
		protected View getTitleView(String title, int index, View convertView, ViewGroup parent) {
			TextView titleView;
			if (convertView == null) {
				titleView = (TextView) getLayoutInflater().inflate(R.layout.vw_sdcard_category_title, null);
			} else {
				titleView = (TextView) convertView;
			}
			titleView.setText(title);
			return titleView;
		}
	};

	/**
	 * 扫描文件适配器.
	 */
	class ScanFileAdapter extends BaseAdapter {

		/** The m context. */
		private Context mContext;

		/** The m file name list. */
		private List<String> mFileNameList;

		/** The m file path list. */
		private List<File> mFilePathList;

		/**
		 * Instantiates a new file adapter.
		 * 
		 * @param context
		 *            the context
		 * @param fileName
		 *            the file name
		 * @param filePath
		 *            the file path
		 */
		public ScanFileAdapter(Context context, List<String> fileName, List<File> filePath) {
			mContext = context;
			mFileNameList = fileName;
			mFilePathList = filePath;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		public int getCount() {
			return mFilePathList.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		public Object getItem(int position) {
			return mFileNameList.get(position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		public long getItemId(int position) {
			return position;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup viewgroup) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater layoutInflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.vw_sdcard_file_list_item, null);
				viewHolder.mIconImageView = (ImageView) convertView.findViewById(R.id.image_list_childs);
				viewHolder.mFileNameTextView = (TextView) convertView.findViewById(R.id.text_list_childs);
				viewHolder.mFileInfoTextView = (TextView) convertView.findViewById(R.id.text_file_info);
				viewHolder.mFileArrowButton = (TextView) convertView.findViewById(R.id.image_file_arrow);
				viewHolder.mFileArrowLayout = (RelativeLayout) convertView.findViewById(R.id.image_file_layout);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final File file = mFilePathList.get(position);
			final String fileName = file.getName();

			viewHolder.mFileNameTextView.setText(fileName);

			final String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase(
					Locale.CHINA);// 取出文件后缀名并转成小写
			if (fileEnds.equals("txt")) {
				viewHolder.mFileInfoTextView.setVisibility(View.VISIBLE);
				viewHolder.mFileInfoTextView.setText("txt " + Util.formatFileSize(file.length()));
				viewHolder.mIconImageView.setBackgroundResource(R.drawable.txt);
			} else if (fileEnds.equals("epub")) {
				viewHolder.mFileInfoTextView.setVisibility(View.VISIBLE);
				viewHolder.mFileInfoTextView.setText("epub " + Util.formatFileSize(file.length()));
				viewHolder.mIconImageView.setBackgroundResource(R.drawable.epub);
			}

			final Book book = new Book();
			book.getDownloadInfo().setFilePath(file.getAbsolutePath());
			book.getDownloadInfo().setOriginalFilePath(file.getAbsolutePath());
			if (DownBookManager.getInstance().hasBook(book)) {
				viewHolder.mFileArrowButton.setBackgroundDrawable(null);
				viewHolder.mFileArrowButton.setText(R.string.has_join);
				viewHolder.mFileArrowLayout.setClickable(false);
			} else {
				viewHolder.mFileArrowButton.setVisibility(View.VISIBLE);
				viewHolder.mFileArrowButton.setBackgroundResource(R.drawable.add_file_button);
				viewHolder.mFileArrowButton.setText("");
				viewHolder.mFileArrowLayout.setClickable(true);
				viewHolder.mFileArrowLayout.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						joinFileToShelves(ScanFileAdapter.this, fileEnds, file);
					}
				});
			}
			return convertView;
		}

		/**
		 * The Class ViewHolder.
		 */
		class ViewHolder {

			/** The m icon image view. */
			ImageView mIconImageView;

			/** The m file name text view. */
			TextView mFileNameTextView;

			/** The m file info text view. */
			TextView mFileInfoTextView;

			/** The m file arrow image view. */
			TextView mFileArrowButton;

			/** The m file arrow image layout. */
			RelativeLayout mFileArrowLayout;
		}
	}

	/**
	 * sd卡文件适配器.
	 */
	class FileAdapter extends BaseAdapter {

		/** The m context. */
		private Context mContext;

		/** The m file name list. */
		private List<String> mFileNameList;

		/** The m file path list. */
		private List<File> mFilePathList;

		/**
		 * Instantiates a new file adapter.
		 * 
		 * @param context
		 *            the context
		 * @param fileName
		 *            the file name
		 * @param filePath
		 *            the file path
		 */
		public FileAdapter(Context context, List<String> fileName, List<File> filePath) {
			mContext = context;
			mFileNameList = fileName;
			mFilePathList = filePath;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getCount()
		 */
		public int getCount() {
			return mFilePathList.size();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItem(int)
		 */
		public Object getItem(int position) {
			return mFileNameList.get(position);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getItemId(int)
		 */
		public long getItemId(int position) {
			return position;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see android.widget.Adapter#getView(int, android.view.View,
		 * android.view.ViewGroup)
		 */
		public View getView(int position, View convertView, ViewGroup viewgroup) {
			ViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				LayoutInflater layoutInflater = (LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = layoutInflater.inflate(R.layout.vw_sdcard_file_list_item, null);
				viewHolder.mIconImageView = (ImageView) convertView.findViewById(R.id.image_list_childs);
				viewHolder.mFileNameTextView = (TextView) convertView.findViewById(R.id.text_list_childs);
				viewHolder.mFileInfoTextView = (TextView) convertView.findViewById(R.id.text_file_info);
				viewHolder.mFileArrowButton = (TextView) convertView.findViewById(R.id.image_file_arrow);
				viewHolder.mFileArrowLayout = (RelativeLayout) convertView.findViewById(R.id.image_file_layout);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}
			final File file = mFilePathList.get(position);

			if (mFileNameList.get(position).toString().equals("BackToParent")) {
				viewHolder.mIconImageView.setBackgroundResource(R.drawable.up_arrow_button);
				viewHolder.mFileNameTextView.setText(R.string.back_to_parent_dir);
				viewHolder.mFileInfoTextView.setVisibility(View.GONE);
				viewHolder.mFileArrowButton.setVisibility(View.GONE);
			} else {
				String fileName = file.getName();
				viewHolder.mFileNameTextView.setText(fileName);
				if (file.isDirectory()) {
					if (file.canRead()) {
						File[] fileLists = listFiles(file, new TXTOrEPUBOrDIRSelector());
						viewHolder.mFileInfoTextView.setVisibility(View.VISIBLE);
						viewHolder.mFileInfoTextView.setText(String.format(getString(R.string.dir_number),
								fileLists.length));
					} else {
						viewHolder.mFileInfoTextView.setVisibility(View.VISIBLE);
						viewHolder.mFileInfoTextView.setText(getString(R.string.do_not_have_access));
					}
					viewHolder.mFileArrowButton.setVisibility(View.VISIBLE);
					viewHolder.mFileArrowButton.setText("");
					viewHolder.mFileArrowLayout.setClickable(false);
					viewHolder.mFileArrowButton.setBackgroundResource(R.drawable.right_arrow);
					viewHolder.mIconImageView.setBackgroundResource(R.drawable.folder);
				} else {
					viewHolder.mFileArrowLayout.setClickable(true);
					final String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length())
							.toLowerCase(Locale.CHINA);// 取出文件后缀名并转成小写
					if (fileEnds.equals("txt")) {
						viewHolder.mFileInfoTextView.setVisibility(View.VISIBLE);
						viewHolder.mFileInfoTextView.setText("txt " + Util.formatFileSize(file.length()));
						viewHolder.mIconImageView.setBackgroundResource(R.drawable.txt);
					} else if (fileEnds.equals("epub")) {
						viewHolder.mFileInfoTextView.setVisibility(View.VISIBLE);
						viewHolder.mFileInfoTextView.setText("epub " + Util.formatFileSize(file.length()));
						viewHolder.mIconImageView.setBackgroundResource(R.drawable.epub);
					}
					viewHolder.mFileArrowButton.setVisibility(View.VISIBLE);
					Book book = new Book();
					book.getDownloadInfo().setFilePath(file.getAbsolutePath());
					book.getDownloadInfo().setOriginalFilePath(file.getAbsolutePath());
					if (DownBookManager.getInstance().hasBook(book)) {
						viewHolder.mFileArrowButton.setBackgroundDrawable(null);
						viewHolder.mFileArrowButton.setText(R.string.has_join);
						viewHolder.mFileArrowLayout.setClickable(false);
					} else {
						viewHolder.mFileArrowButton.setBackgroundResource(R.drawable.add_file_button);
						viewHolder.mFileArrowButton.setText("");
						viewHolder.mFileArrowLayout.setClickable(true);
						viewHolder.mFileArrowLayout.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								joinFileToShelves(FileAdapter.this, fileEnds, file);
							}
						});
					}
				}
			}
			return convertView;
		}

		/**
		 * The Class ViewHolder.
		 */
		class ViewHolder {

			/** The m icon image view. */
			ImageView mIconImageView;

			/** The m file name text view. */
			TextView mFileNameTextView;

			/** The m file info text view. */
			TextView mFileInfoTextView;

			/** The m file arrow image view. */
			TextView mFileArrowButton;

			/** The m file arrow image layout. */
			RelativeLayout mFileArrowLayout;
		}
	}

	/**
	 * 文件夹过滤器.
	 */
	class DirSelector implements FileFilter {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File pathname) {
			if (pathname.canRead() && pathname.isDirectory()) {
				return true;
			}
			return false;
		}
	}

	/**
	 * txt和epub文件的过滤器.
	 */
	class TXTOrEPUBSelector implements FileFilter {

		/** The txt end. */
		String txtEnd = ".txt";

		/** The epub end. */
		String epubEnd = ".epub";

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File pathname) {
			if (pathname.isFile()
					&& pathname.length() >= 5 * 1024
					&& pathname.canRead()
					&& (pathname.getName().toLowerCase(Locale.CHINA).endsWith(txtEnd) || pathname.getName()
							.toLowerCase(Locale.CHINA).endsWith(epubEnd))) {
				return true;
			}
			return false;
		}
	}

	/**
	 * txt,epub和文件夹且带有文件大小限制的过滤器.
	 */
	class TXTOrEPUBOrDIRLengthLimitSelector implements FileFilter {

		/** The txt end. */
		String txtEnd = ".txt";

		/** The epub end. */
		String epubEnd = ".epub";

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory() && pathname.canRead()) {
				return true;
			} else {
				if (pathname.length() > SCAN_FILE_SIZE_LIMIT && pathname.canRead()
						&& pathname.getName().toLowerCase(Locale.CHINA).endsWith(txtEnd)
						|| pathname.getName().toLowerCase(Locale.CHINA).endsWith(epubEnd)) {
					return true;
				}
			}
			return false;
		}

	}

	/**
	 * txt,epub和文件夹的过滤器.
	 */
	class TXTOrEPUBOrDIRSelector implements FileFilter {

		/** The txt end. */
		String txtEnd = ".txt";

		/** The epub end. */
		String epubEnd = ".epub";

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.io.FileFilter#accept(java.io.File)
		 */
		@Override
		public boolean accept(File pathname) {
			if (pathname.isDirectory() && pathname.canRead()) {
				return true;
			} else {
				if (pathname.length() > 0 && pathname.canRead()
						&& pathname.getName().toLowerCase(Locale.CHINA).endsWith(txtEnd)
						|| pathname.getName().toLowerCase(Locale.CHINA).endsWith(epubEnd)) {
					return true;
				}
			}
			return false;
		}

	}
}
