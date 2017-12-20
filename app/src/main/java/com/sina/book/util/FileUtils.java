package com.sina.book.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.os.Environment;
import android.os.StatFs;

import com.sina.book.SinaBookApplication;

public class FileUtils
{

	public synchronized static File checkAndCreateFile(String fileName)
	{
		return checkOrCreateFile(fileName, true);
	}

	// 检测并创建文件
	public synchronized static File checkOrCreateFile(String fileName, boolean isCreate)
	{
		if (fileName == null) {
			return null;
		}

		File file = new File(fileName);
		if (file != null && !file.exists()) {
			try {
				if (isCreate) {
					String parentPath = file.getParent();
					boolean result = fileProberParent(parentPath);
					if (result) {
						long size = getAvailableExternalMemorySize();
						if (size != -1) {
							file.createNewFile();
							return file;
						}
					} else {
						file = null;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				// Log.e("FileUtil", "检测并创建文件 >> checkOrCreateFile e:" + e);
				file = null;
			}
		}
		return file;
	}

	// 递归创建父级目录
	private synchronized static boolean fileProberParent(String dirPath)
	{
		if (dirPath == null) {
			return false;
		}
		File file = new File(dirPath);
		if (file == null || !file.exists()) {
			// 文件夹不存在，递归寻找上级目录
			String parentPath = file.getParent();
			boolean result = fileProberParent(parentPath);
			if (result) {
				file.mkdir();
			}
			return result;
		}
		return true;
	}

	// 获取存储卡剩余空间
	public synchronized static long getAvailableExternalMemorySize()
	{
		boolean sdcard = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if (sdcard) {
			File path = Environment.getExternalStorageDirectory();
			StatFs stat = new StatFs(path.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			long formatSize = availableBlocks * blockSize;
			// long formatSize = (availableBlocks * blockSize) / (1024 * 1024);
			return formatSize;
		} else {
			return -1;
		}
	}

	// 删除文件或文件夹
	public synchronized static void deleteFile(String path)
	{
		if (path == null) {
			return;
		}

		File file = new File(path);
		if (file != null && file.exists()) {
			if (file.isDirectory()) {
				String[] list = file.list();
				if(list != null && list.length > 0){
					for (int i = 0; i < list.length; ++i) {
						String tmpPath = path + "/" + list[i];
						deleteFile(tmpPath);
					}
				}
				file.delete();
			} else if (file.isFile()) {
				file.delete();
			}
		}
	}

	/**
	 * assets目录下的文件是否存在
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean assertsFileExist(String filePath)
	{
		try {
			String fileName = filePath.substring(filePath.lastIndexOf('/'));
			InputStream myInput = SinaBookApplication.gContext.getAssets().open("book" + fileName);
			myInput.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static byte[] readData(String path)
	{
		File file = checkOrCreateFile(path, false);
		return readData(file);
	}

	public static byte[] readData(File file)
	{
		if (file == null) {
			return null;
		}
		if (!file.exists()) {
			return null;
		}

		// 文件输入流对象
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			// 文件流总大小
			int size = fis.available();
			// 存储数据的字节数组
			byte[] data = new byte[size];
			// 读取数据
			fis.read(data, 0, data.length);
			return data;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fis = null;
			}
		}
		return null;
	}
	
	public static boolean writeData(String path, byte[] data) {
		File file = checkOrCreateFile(path, true);
		if (file != null && file.exists()) {
			return writeData(file, data);
		}
		return false;
	}
	
	public static boolean writeData(File file, byte[] data) {
		if (file == null) {
			return false;
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file,false);
			fos.write(data);
			fos.flush();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				fos = null;
			}
		}
		return true;
	}
	
	
}
