/*
 * Copyright (C) 2007-2014 Geometer Plus <contact@geometerplus.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301, USA.
 */

package org.geometerplus.zlibrary.core.filesystem;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.geometerplus.zlibrary.core.drm.EncryptionMethod;
import org.geometerplus.zlibrary.core.drm.FileEncryptionInfo;
import org.geometerplus.zlibrary.core.drm.embedding.EmbeddingInputStream;
import org.geometerplus.zlibrary.core.util.InputStreamHolder;
import org.geometerplus.zlibrary.ui.android.library.ZLAndroidLibrary;

/**
 * 代表文件的抽象类<br>
 * 1.{@link ZLResourceFile}: 代表程序资源文件的抽象类，如assets下的文件，是个抽象类<br>
 * 2.{@link ZLAndroidLibrary.AndroidAssetsFile}: 代表/assets资源下的文件<br>
 * 3.{@link ZLPhysicalFile}: 代表物理磁盘文件<br>
 * 4.{@link ZLArchiveEntryFile}: 代表压缩包文件<br>
 * 
 * @author chenjl
 * 
 */
public abstract class ZLFile implements InputStreamHolder
{
	/**
	 * 缓存文件集合
	 */
	private final static HashMap<String, ZLFile>	ourCachedFiles	= new HashMap<String, ZLFile>();

	/**
	 * 压缩文件类型
	 * 
	 * @author chenjl
	 * 
	 */
	protected interface ArchiveType
	{
		int	NONE		= 0;		// 未知类型
		int	GZIP		= 0x0001;
		int	BZIP2		= 0x0002;
		int	COMPRESSED	= 0x00ff;	// 压缩
		int	ZIP			= 0x0100;
		int	TAR			= 0x0200;
		int	ARCHIVE		= 0xff00;	// 存档
	};

	/**
	 * 文件的扩展名
	 */
	private String	myExtension;
	/**
	 * 文件名
	 */
	private String	myShortName;
	/**
	 * 压缩包文件类型
	 */
	protected int	myArchiveType;
	/**
	 * 是否缓存过了
	 */
	private boolean	myIsCached;

	/**
	 * 初始化后缀名，文件名，文件类型
	 */
	protected void init()
	{
		final String name = getLongName();
		final int index = name.lastIndexOf('.');
		myExtension = (index > 0) ? name.substring(index + 1).toLowerCase()
				.intern() : "";
		myShortName = name.substring(name.lastIndexOf('/') + 1);

		/*
		 * if (lowerCaseName.endsWith(".gz")) { myNameWithoutExtension =
		 * myNameWithoutExtension.substring(0, myNameWithoutExtension.length() -
		 * 3); lowerCaseName = lowerCaseName.substring(0, lowerCaseName.length()
		 * - 3); myArchiveType = myArchiveType | ArchiveType.GZIP; } if
		 * (lowerCaseName.endsWith(".bz2")) { myNameWithoutExtension =
		 * myNameWithoutExtension.substring(0, myNameWithoutExtension.length() -
		 * 4); lowerCaseName = lowerCaseName.substring(0, lowerCaseName.length()
		 * - 4); myArchiveType = myArchiveType | ArchiveType.BZIP2; }
		 */
		int archiveType = ArchiveType.NONE;
		if (myExtension == "zip") {
			archiveType |= ArchiveType.ZIP;
		} else if (myExtension == "oebzip") {
			archiveType |= ArchiveType.ZIP;
		} else if (myExtension == "epub") {
			archiveType |= ArchiveType.ZIP;
		} else if (myExtension == "tar") {
			archiveType |= ArchiveType.TAR;
			// } else if (lowerCaseName.endsWith(".tgz")) {
			// nothing to-do myNameWithoutExtension =
			// myNameWithoutExtension.substr(0, myNameWithoutExtension.length()
			// - 3) + "tar";
			// myArchiveType = myArchiveType | ArchiveType.TAR |
			// ArchiveType.GZIP;
		}
		myArchiveType = archiveType;
	}

	/**
	 * 创建文件
	 * 
	 * @param parent
	 * @param name
	 * @return
	 */
	public static ZLFile createFile(ZLFile parent, String name)
	{
		ZLFile file = null;
		if (parent == null) {
			ZLFile cached = ourCachedFiles.get(name);
			if (cached != null) {
				return cached;
			}
			// 非"/"开头的文件，判给APP资源ZLResourceFile类
			if (name.length() == 0 || name.charAt(0) != '/') {
				return ZLResourceFile.createResourceFile(name);
			} else {
				// 物理文件
				return new ZLPhysicalFile(name);
			}
		} else if (parent instanceof ZLPhysicalFile
				&& (parent.getParent() == null)) {
			// parent is a directory
			// parent是个文件夹，组合parent路径+"/"+文件名作为路径返回
			file = new ZLPhysicalFile(parent.getPath() + '/' + name);
		} else if (parent instanceof ZLResourceFile) {
			file = ZLResourceFile.createResourceFile((ZLResourceFile) parent,
					name);
		} else {
			file = ZLArchiveEntryFile.createArchiveEntryFile(parent, name);
		}
		
		if (!ourCachedFiles.isEmpty() && file != null) {
			ZLFile cached = ourCachedFiles.get(file.getPath());
			if (cached != null) {
				return cached;
			}
		}
		return file;
	}

	public static ZLFile createFileByUrl(String url)
	{
		// 以file://开头的文件路径
		if (url == null || !url.startsWith("file://")) {
			return null;
		}
		return createFileByPath(url.substring("file://".length()));
	}

	/**
	 * 传入路径得到ZLFile对象
	 * 
	 * @param path
	 * @return
	 */
	public static ZLFile createFileByPath(String path)
	{
		if (path == null) {
			return null;
		}
		// 首先从缓存中获取
		ZLFile cached = ourCachedFiles.get(path);
		if (cached != null) {
			return cached;
		}

		// 获取路径首字符
		int len = path.length();
		char first = len == 0 ? '*' : path.charAt(0);
		// 不是以"/"开头的路径
		if (first != '/') {
			// 一直遍历查找下去，一直到开头不是"./"了为止
			while (len > 1 && first == '.' && path.charAt(1) == '/') {
				// 截取"./"后面的部分，重新设置len和first的值
				path = path.substring(2);
				len -= 2;
				first = len == 0 ? '*' : path.charAt(0);
				// 继续while循环判定
			}
			return ZLResourceFile.createResourceFile(path);
		}
		// 如：/storage/emulated/0/Download/南方周末20140103_4717491b.epub.epub:OEBPS/Images/image030.jpeg
		int index = path.lastIndexOf(':');
		if (index > 1) {
			// 以"/"开头，带":"的路径
			final ZLFile archive = createFileByPath(path.substring(0, index));
			if (archive != null && archive.myArchiveType != 0) {
				// 压缩包文件
				return ZLArchiveEntryFile.createArchiveEntryFile(archive,
						path.substring(index + 1));
			}
		}
		// 存储介质中的实体文件
		return new ZLPhysicalFile(path);
	}

	public abstract long size();

	public abstract boolean exists();

	public abstract boolean isDirectory();

	public abstract String getPath();

	public abstract ZLFile getParent();

	/**
	 * 物理文件(存在于存储介质中的文件)
	 * 
	 * @return
	 */
	public abstract ZLPhysicalFile getPhysicalFile();

	public abstract InputStream getInputStream() throws IOException;

	/**
	 * 文件最后的修改时间，只有物理文件(存在于存储介质中的文件)才有这个属性
	 * 
	 * @return
	 */
	public long lastModified()
	{
		final ZLFile physicalFile = getPhysicalFile();
		return physicalFile != null ? physicalFile.lastModified() : 0;
	}

	public final InputStream getInputStream(FileEncryptionInfo encryptionInfo)
			throws IOException
	{
		if (encryptionInfo == null) {
			return getInputStream();
		}

		if (EncryptionMethod.EMBEDDING.equals(encryptionInfo.Method)) {
			return new EmbeddingInputStream(getInputStream(),
					encryptionInfo.ContentId);
		}

		throw new IOException("Encryption method " + encryptionInfo.Method
				+ " is not supported");
	}

	public String getUrl()
	{
		return "file://" + getPath();
	}

	public boolean isReadable()
	{
		return true;
	}

	public final boolean isCompressed()
	{
		return (0 != (myArchiveType & ArchiveType.COMPRESSED));
	}

	public final boolean isArchive()
	{
		return (0 != (myArchiveType & ArchiveType.ARCHIVE));
	}

	public abstract String getLongName();

	public final String getShortName()
	{
		return myShortName;
	}

	public final String getExtension()
	{
		return myExtension;
	}

	protected List<ZLFile> directoryEntries()
	{
		return Collections.emptyList();
	}

	/**
	 * 子集
	 * 
	 * @return
	 */
	public final List<ZLFile> children()
	{
		if (exists()) {
			if (isDirectory()) {
				return directoryEntries();
			} else if (isArchive()) {
				return ZLArchiveEntryFile.archiveEntries(this);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public int hashCode()
	{
		return getPath().hashCode();
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == this) {
			return true;
		}
		if (!(o instanceof ZLFile)) {
			return false;
		}
		return getPath().equals(((ZLFile) o).getPath());
	}

	@Override
	public String toString()
	{
		return "ZLFile [" + getPath() + "]";
	}

	protected boolean isCached()
	{
		return myIsCached;
	}

	/**
	 * 设置缓存与否
	 * 
	 * @param cached
	 */
	public void setCached(boolean cached)
	{
		myIsCached = cached;
		if (cached) {
			ourCachedFiles.put(getPath(), this);
		} else {
			ourCachedFiles.remove(getPath());
			if (0 != (myArchiveType & ArchiveType.ZIP)) {
				ZLZipEntryFile.removeFromCache(this);
			}
		}
	}
}
