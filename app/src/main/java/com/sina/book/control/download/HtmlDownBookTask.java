package com.sina.book.control.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kxml3.io.HTMLParser;

import android.os.Handler;
import android.util.Log;

import com.sina.book.DesUtils;
import com.sina.book.SinaBookApplication;
import com.sina.book.control.TaskParams;
import com.sina.book.data.ConstantData;
import com.sina.book.util.FileUtils;
import com.sina.book.util.HttpUtil;
import com.sina.book.util.StorageUtil;

/**
 * 下载xhtml的事务
 */
public class HtmlDownBookTask extends DownFileTask
{
	public static final Pattern	PATTERN			= Pattern.compile("<img\\s+(?:[^>]*)src\\s*=\\s*([^>]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	
	public static final Pattern	PATTERN_LINK	= Pattern.compile("<link\\s+(?:[^>]*)href\\s*=\\s*([^>]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	public static final int		MAX_LENGTH		= 10 * 1024;

	private HtmlDownJob			mJob;

	// 下载地址

	public String				bookId;

	public String				chapterId;
	
	private Handler handler = new Handler();

	public HtmlDownBookTask(HtmlDownJob job)
	{
		super("", 0);
		mJob = job;
		mUrl = mJob.getUrl();

		bookId = mJob.bookId;
		chapterId = mJob.chapterId;

		mTempFilePath = mJob.mananger.mBookDir + "/OEBPS/text/" + chapterId + ".xhtml";
	}

	public HtmlDownJob getJob()
	{
		return mJob;
	}

	public void setJob(HtmlDownJob job)
	{
		this.mJob = job;
	}

	/**
	 * 同步执行<br>
	 * 请不要在UI线程使用该方法<br>
	 * 
	 * @return
	 */
	public final DownResult syncExecute(TaskParams... params)
	{
		return doInBackground(params);
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning)
	{
		// if (mGetChaptersTask != null) {
		// mGetChaptersTask.abort();
		// }
		return super.cancel(mayInterruptIfRunning);
	}

	@Override
	protected void onProgressUpdate(Object... values)
	{
		// if (mFileSize > 0) {
		// DownloadBookNotification.getInstance().updateNotification(mJob.getBook());
		// mJob.getBook().getDownloadInfo().setProgress(getProgress());
		// mJob.getBook().getDownloadInfo().setFileSize(mFileSize);
		// }
		// if (mJob.getBook().getDownloadInfo().getProgress() > 1.0) {
		// mJob.getBook().getDownloadInfo().setProgress(1.0);
		// }
		// mJob.getBook().getDownloadInfo().setParsering(mIsWriting);
		// long timePos = System.currentTimeMillis();
		// // 1000ms才会去回调一次，防止书架界面更新太多次，导致闪烁
		// if (timePos - mLastPos > MAX_REFRESH_TIME) {
		// DownBookManager.getInstance().onTaskUpdate(mJob.getBook(), false,
		// true, DownBookJob.STATE_RUNNING);
		// mLastPos = timePos;
		// }
		super.onProgressUpdate(values);
	}

	// 下载任务
	protected DownResult doInBackground(TaskParams... params)
	{
		cancelable = true;
		if (isCancelled()) {
			mResult.stateCode = 1;
			return mResult;
		}

		// 1、检查文件是否存在
		File file = FileUtils.checkOrCreateFile(mTempFilePath, false);
		boolean isFileExists = false;
		
		byte[] htmlData = null;
		if (file != null && file.exists()) {
			htmlData = FileUtils.readData(mTempFilePath);
			if(htmlData != null && htmlData.length > 0){
				isFileExists = true;
			}
		}
		
		if(!isFileExists){
			// 文件不存在 或者文件为0k
			boolean flag = downloadData(true);
			if (!flag) {
				// 文件下载失败
//				Log.e("ouyang", "HtmlDownTask---doInBackground--html下载失败");
				mResult.stateCode = -1;
				mResult.retObj = "下载失败";
				return mResult;
			}else{
//				Log.d("ouyang", "HtmlDownTask---doInBackground--html下载成功");
				mResult.stateCode = 0;
				mResult.retObj = "成功";
			}
			htmlData = FileUtils.readData(mTempFilePath);
		}else{
//			Log.d("ouyang", "HtmlDownTask---doInBackground--html本地存在，成功");
			mResult.stateCode = 0;
			mResult.retObj = "成功";
		}
		
		if (isCancelled()) {
//			Log.i("ouyang", "HtmlDownTask---doInBackground--html下载被取消");
			return null;
//			mResult.stateCode = 1;
//			htmlData = null;
//			return mResult;
		}
//		if (file == null || !file.exists()) {
//			// 文件不存在，开始下载
//			boolean flag = downloadData(true);
//			if (!flag) {
//				// 文件下载失败
//				mResult.stateCode = -1;
//				mResult.retObj = "下载失败";
//				return mResult;
//			}else{
//				mResult.stateCode = 0;
//				mResult.retObj = "成功";
//			}
//			
//			if (isCancelled()) {
//				mResult.stateCode = 1;
//				return mResult;
//			}
//		} else {
//			// 文件存在
//			mResult.stateCode = 0;
//			mResult.retObj = "成功";
//		}
//		byte[] htmlData = FileUtils.readData(mTempFilePath);
		
		if(htmlData == null || htmlData.length == 0){
			Log.e("ouyang", "HtmlDownTask---doInBackground--html下载失败--data数据为空");
			mResult.stateCode = -1;
			mResult.retObj = "下载失败";
			return mResult;
		}
		
		try {
			html = new String(htmlData, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			html = new String(htmlData);
		}
		
		// 2、扫描html文件内的图片和css
		ArrayList<ResourceDownJob> resList = parseAttr();
		
		// 3、资源文件都存在，直接返回
		if(resList == null || resList.size() == 0){
			// 没有资源需要下载
			if(hasHttpAttr){
				// 有路径需要修改,onState，替换路径
//				Log.i("ouyang", "--HtmlDownTask--doINbackground--资源存在-有路径修改-- ");
				handler.post(new Runnable()
				{
					public void run()
					{
						mJob.mananger.onState(chapterId, true, 0);
					}
				});
			}else{
				// 已经修改过，调用onState，不替换路径
//				Log.i("ouyang", "--HtmlDownTask--doINbackground--资源存在-不替换路径-- ");
				handler.post(new Runnable()
				{
					public void run()
					{
						mJob.mananger.onState(chapterId, false, 0);
					}
				});
			}
		}else{
			//有资源下载,先去下载。
			// 4、注册资源管理下载
			mJob.mananger.mResourceInstance.registerResDownload(chapterId, resList);
		}

			// 3、 替换http的url为本地相对url。
//			boolean isError = false;
//			if (!isFileExits) {
//				// 本地不存在，存储html
//				File descFile = new File(mTempFilePath);
//				if (descFile == null || !descFile.exists()) {
//					FileUtils.checkAndCreateFile(mTempFilePath);
//				}
//				FileOutputStream fos = null;
//				try {
//					fos = new FileOutputStream(mTempFilePath);
//					byte[] tmpData = null;
//					try {
//						tmpData = html.getBytes("UTF-8");
//					} catch (Exception e) {
//						tmpData = html.getBytes();
//					}
//					fos.write(tmpData);
//					fos.flush();
//
//					mResult.stateCode = 0;
//					mResult.retObj = "成功";
//				} catch (FileNotFoundException e) {
//					isError = true;
//					e.printStackTrace();
//				} catch (IOException e) {
//					isError = true;
//					FileUtils.deleteFile(mTempFilePath);
//					e.printStackTrace();
//				} finally {
//					if (fos != null) {
//						try {
//							fos.close();
//						} catch (IOException e) {
//							e.printStackTrace();
//						}
//						fos = null;
//					}
//				}
//			}
//			
//			// 4、注册资源管理下载
//			if (!isError) {
//				ResourceDownManager.getInstance().registerResDownload(mUrl, resList);
//			}
		return mResult;
	}

	private String	html;

	// 正则查找img的src属性
	private ArrayList<ResourceDownJob> parseAttr()
	{
		ArrayList<ResourceDownJob> resList = new ArrayList<ResourceDownJob>();

		// 查找CSS
		Matcher matcher = PATTERN_LINK.matcher(html);
		findAttr(resList, matcher, ResourceDownJob.TYPE_CSS);

		// 查找图片src
		Matcher matcher2 = PATTERN.matcher(html);
		findAttr(resList, matcher2, ResourceDownJob.TYPE_IMAGE);
		return resList;
	}

	// html中是否含有http的url
	private boolean hasHttpAttr = false;
	
	
	private boolean findAttr(ArrayList<ResourceDownJob> list, Matcher matcher, int type)
	{
		while (matcher.find()) {
			String group = matcher.group(1);
			if (group == null) {
				continue;
			}

			// 这里可能还需要更复杂的判断,用以处理src="...."内的一些转义符
			String src = "";
			if (group.startsWith("'")) {
				src = group.substring(1, group.indexOf("'", 1));
			} else if (group.startsWith("\"")) {
				src = group.substring(1, group.indexOf("\"", 1));
			} else {
				src = group.split("\\s")[0];
			}
			
			if(src.startsWith("http://")){
				hasHttpAttr = true;
				
				String fileName = DesUtils.encryptString(src);
//				String fileName = Util.md5(src);
				
				// 检测本地是否已经下载过
				String absPath = "";
				OPFData opfData = new OPFData();
				if (type == ResourceDownJob.TYPE_IMAGE) {
					absPath = mJob.mananger.mBookDir + "/OEBPS/images/" + fileName;
					opfData.href = "images/" + fileName;
					opfData.id = fileName;
//					absPath = mJob.mananger.mBookDir + "/OEBPS/images/" + fileName + ".jpg";
//					opfData.href = "images/" + fileName + ".jpg";
//					opfData.id = fileName + ".jpg";
					opfData.media_type = OPFData.MEDIA_TYPE_JPEG;
				} else if (type == ResourceDownJob.TYPE_CSS) {
					absPath = mJob.mananger.mBookDir + "/OEBPS/styles/" + fileName;
					opfData.href = "images/" + fileName;
					opfData.id = fileName;
//					absPath = mJob.mananger.mBookDir + "/OEBPS/styles/" + fileName + ".css";
//					opfData.href = "images/" + fileName + ".css";
//					opfData.id = fileName + ".css";
					opfData.media_type = OPFData.MEDIA_TYPE_CSS;
				}
				
				// 1、先检测是否已经添加过，如果已经添加则不用重复添加下载。
				if(mJob.mananger.opfResList.get(opfData.id) == null){
					mJob.mananger.opfResList.put(opfData.id, opfData);
					File file = FileUtils.checkOrCreateFile(absPath, false);
					if (file == null || !file.exists()) {
						// 添加到下载队列
						ResourceDownJob job = new ResourceDownJob(mJob.mananger.mResourceInstance, src, type, bookId, chapterId);
						list.add(job);
					}
				}
			}else{
				// 资源已经下载并替换过html, 还原复检资源是否存在
				int tIndex = src.lastIndexOf('/');
				String fileName = src.substring(tIndex + 1);
				String url =  DesUtils.decryptString(fileName);
				
				// 检测本地是否已经下载过
				String absPath = "";
				OPFData opfData = new OPFData();
				if (type == ResourceDownJob.TYPE_IMAGE) {
					absPath = mJob.mananger.mBookDir + "/OEBPS/images/" + fileName;
					opfData.href = "images/" + fileName;
					opfData.id = fileName;
					opfData.media_type = OPFData.MEDIA_TYPE_JPEG;
				} else if (type == ResourceDownJob.TYPE_CSS) {
					absPath = mJob.mananger.mBookDir + "/OEBPS/styles/" + fileName;
					opfData.href = "images/" + fileName;
					opfData.id = fileName;
					opfData.media_type = OPFData.MEDIA_TYPE_CSS;
				}
				
				// 1、先检测是否已经添加过，如果已经添加则不用重复添加下载。
				if(mJob.mananger.opfResList.get(opfData.id) == null){
					mJob.mananger.opfResList.put(opfData.id, opfData);
					File file = FileUtils.checkOrCreateFile(absPath, false);
					if (file == null || !file.exists()) {
						// 添加到下载队列
						ResourceDownJob job = new ResourceDownJob(mJob.mananger.mResourceInstance, url, type, bookId, chapterId);
						list.add(job);
					}
				}
				
				
				
				
//				OPFData opfData = new OPFData();
//				if (type == ResourceDownJob.TYPE_IMAGE) {
//					opfData.href = "images/" + fileName;
//					opfData.id = fileName;
//					opfData.media_type = OPFData.MEDIA_TYPE_JPEG;
//				} else if (type == ResourceDownJob.TYPE_CSS) {
//					opfData.href = "images/" + fileName;
//					opfData.id = fileName;
//					opfData.media_type = OPFData.MEDIA_TYPE_CSS;
//				}
//				mJob.mananger.opfResList.put(opfData.id, opfData);
			}
		}
		return true;
	}
	
	public static void replaceHtml(String bookId, String chapterId){
		String baseDir = StorageUtil.getDirByType(StorageUtil.DIR_TYPE_BOOK);
		String bookDirName = "/bd" + bookId + ".epub";
		String path = baseDir + bookDirName + "/OEBPS/text/" + chapterId + ".xhtml";
		
		byte[] data = FileUtils.readData(path);
		if(data == null){
			return;
		}
		
		String html = "";
		try {
			html = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			html = new String(data);
		}
		
		Matcher matcher = PATTERN_LINK.matcher(html);
		while (matcher.find()) {
			String group = matcher.group(1);
			if (group == null) {
				continue;
			}

			// 这里可能还需要更复杂的判断,用以处理src="...."内的一些转义符
			String src = "";
			if (group.startsWith("'")) {
				src = group.substring(1, group.indexOf("'", 1));
			} else if (group.startsWith("\"")) {
				src = group.substring(1, group.indexOf("\"", 1));
			} else {
				src = group.split("\\s")[0];
			}
			
			if(src.startsWith("http://")){
				String fileName = DesUtils.encryptString(src);
//				String fileName = Util.md5(src) + ".css";
				
				// 替换路径
				String tpath = "../styles/" + fileName;
				html = html.replaceAll(src, tpath);
			}
		}
		
		matcher = PATTERN.matcher(html);
		while (matcher.find()) {
			String group = matcher.group(1);
			if (group == null) {
				continue;
			}

			// 这里可能还需要更复杂的判断,用以处理src="...."内的一些转义符
			String src = "";
			if (group.startsWith("'")) {
				src = group.substring(1, group.indexOf("'", 1));
			} else if (group.startsWith("\"")) {
				src = group.substring(1, group.indexOf("\"", 1));
			} else {
				src = group.split("\\s")[0];
			}
			
			if(src.startsWith("http://")){
				String fileName = DesUtils.encryptString(src);
//				String fileName = Util.md5(src) + ".jpg";
				// 替换路径
				String tpath = "../images/" + fileName;
				html = html.replaceAll(src, tpath);
			}
		}
		
		// 存储html
		FileUtils.deleteFile(path);
//		File file = FileUtils.checkOrCreateFile(path, true);
		byte[] htmlData = null;
		try {
			htmlData = html.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			htmlData = html.getBytes();
		}
		FileUtils.writeData(path, htmlData);
	}

	protected HttpURLConnection initHttpConnection(boolean disableAttr) throws IOException
	{
		URL url = new URL(mUrl);
		HttpURLConnection con = HttpUtil.getHttpUrlConnection(url, SinaBookApplication.gContext, disableAttr);
		con.setRequestProperty("Accept-Encoding", "gzip,deflate");
		con.connect();
		return con;
	}

	// 存储文件
	protected boolean writeData2File(InputStream inputStream)
	{
		boolean flag = false;
		FileOutputStream fos = null;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			byte[] data = new byte[BYTE_SIZE];
			int len = 0;
			boolean sdExist = true;
			
			//------------------------
			while ((len = inputStream.read(data, 0, data.length)) != -1) {
				bos.write(data, 0, len);
			}
			
			byte[] byteData = bos.toByteArray();
			HTMLParser parser = new HTMLParser();
			String content = new String(byteData, "utf-8");
			String strResult =  parser.parse(content, "utf-8");
			byteData = strResult.getBytes("UTF-8");
			
			File file = new File(mTempFilePath);
			if (file == null || !file.exists()) {
				FileUtils.checkAndCreateFile(mTempFilePath);
			}
			fos = new FileOutputStream(mTempFilePath);
//			fos.write(byteData);
			if (!StorageUtil.isSDCardExist()) {
				// SD卡不存在
				sdExist = false;
			} else {
				fos.write(byteData);
				mTempFileSize += byteData.length;
			}
			
			//-------------------
			
//			File file = new File(mTempFilePath);
//			if (file == null || !file.exists()) {
//				FileUtils.checkAndCreateFile(mTempFilePath);
//			}
//			fos = new FileOutputStream(mTempFilePath);
//			
//			while ((len = inputStream.read(data, 0, data.length)) != -1) {
//					if (!StorageUtil.isSDCardExist()) {
//						// SD卡不存在
//						sdExist = false;
//						break;
//					} else {
//						fos.write(data, 0, len);
//						mTempFileSize += len;
//					}
//			}
			//-------------------

			if (sdExist) {
				flag = true;
			} else {
				flag = false;
				mResult.stateCode = ConstantData.CODE_FAIL_KEY;
			}
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			FileUtils.deleteFile(mTempFilePath);
		}catch(Exception e){
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
		return flag;
	}
	
}
