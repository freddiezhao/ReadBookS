/*
 * Copyright (C) 2010-2014 Geometer Plus <contact@geometerplus.com>
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

package org.geometerplus.zlibrary.core.network;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 代表网络请求的抽象类
 * 
 * @author chenjl
 * 
 */
public abstract class ZLNetworkRequest {

	// GET请求
	public static abstract class Get extends ZLNetworkRequest {
		protected Get(String url) {
			this(url, false);
		}

		protected Get(String url, boolean quiet) {
			super(url, quiet);
		}
	}

	// POST请求(携带POST参数)
	public static abstract class PostWithMap extends ZLNetworkRequest {
		public final Map<String, String> PostParameters = new HashMap<String, String>();

		protected PostWithMap(String url) {
			this(url, false);
		}

		protected PostWithMap(String url, boolean quiet) {
			super(url, quiet);
		}

		public void addPostParameter(String name, String value) {
			PostParameters.put(name, value);
		}
	}

	// POST请求(携带BODY数据)
	public static abstract class PostWithBody extends ZLNetworkRequest {
		public final String Body;

		protected PostWithBody(String url, String body, boolean quiet) {
			super(url, quiet);
			Body = body;
		}
	}

	// PUT请求(文件上传)
	public static abstract class FileUpload extends ZLNetworkRequest {
		final File File;

		protected FileUpload(String url, File file, boolean quiet) {
			super(url, quiet);
			File = file;
		}
	}

	/**
	 * 请求地址
	 */
	String URL;
	/**
	 * 请求头Map
	 */
	public final Map<String, String> Headers = new HashMap<String, String>();

	private final boolean myIsQuiet;

	private ZLNetworkRequest(String url) {
		this(url, false);
	}

	private ZLNetworkRequest(String url, boolean quiet) {
		URL = url;
		myIsQuiet = quiet;
	}

	/**
	 * 添加头信息
	 * 
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value) {
		Headers.put(name, value);
	}

	public String getURL() {
		return URL;
	}

	public boolean isQuiet() {
		return myIsQuiet;
	}

	/**
	 * 请求前回调
	 * 
	 * @throws ZLNetworkException
	 */
	public void doBefore() throws ZLNetworkException {
	}

	/**
	 * 处理流数据
	 * 
	 * @param inputStream
	 * @param length
	 * @throws IOException
	 * @throws ZLNetworkException
	 */
	public void handleStream(InputStream inputStream, int length)
			throws IOException, ZLNetworkException {
	}

	/**
	 * 请求完成后回调
	 * 
	 * @param success
	 * @throws ZLNetworkException
	 */
	public void doAfter(boolean success) throws ZLNetworkException {
	}
}
