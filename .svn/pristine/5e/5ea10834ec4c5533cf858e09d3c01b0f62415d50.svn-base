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
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.geometerplus.zlibrary.core.options.ZLStringOption;
import org.geometerplus.zlibrary.core.resources.ZLResource;
import org.geometerplus.zlibrary.core.util.MiscUtil;
import org.geometerplus.zlibrary.core.util.ZLNetworkUtil;

/**
 * 网络请求管理器
 * 
 * @author chenjl
 * 
 */
public class ZLNetworkManager {

	/**
	 * Cookie存储接口
	 * 
	 * @author chenjl
	 * 
	 */
	public static interface CookieStore extends
			org.apache.http.client.CookieStore {

		// 清除某域
		void clearDomain(String domain);

		// 重置
		void reset();
	}

	// 单例模式
	private static ZLNetworkManager ourManager;

	public static ZLNetworkManager Instance() {
		if (ourManager == null) {
			ourManager = new ZLNetworkManager();
		}
		return ourManager;
	}

	/**
	 * 授权范围
	 * 
	 * @author chenjl
	 * 
	 */
	private static class AuthScopeKey {

		private final AuthScope myScope;

		public AuthScopeKey(AuthScope scope) {
			myScope = scope;
		}

		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof AuthScopeKey)) {
				return false;
			}

			final AuthScope scope = ((AuthScopeKey) obj).myScope;
			if (myScope == null) {
				return scope == null;
			}
			if (scope == null) {
				return false;
			}
			return myScope.getPort() == scope.getPort()
					&& MiscUtil.equals(myScope.getHost(), scope.getHost())
					&& MiscUtil.equals(myScope.getScheme(), scope.getScheme())
					&& MiscUtil.equals(myScope.getRealm(), scope.getRealm());
		}

		public int hashCode() {
			if (myScope == null) {
				return 0;
			}
			return myScope.getPort() + MiscUtil.hashCode(myScope.getHost())
					+ MiscUtil.hashCode(myScope.getScheme())
					+ MiscUtil.hashCode(myScope.getRealm());
		}
	}

	/**
	 * 证书创造器
	 * 
	 * @author chenjl
	 * 
	 */
	public static abstract class CredentialsCreator {

		final private HashMap<AuthScopeKey, Credentials> myCredentialsMap = new HashMap<AuthScopeKey, Credentials>();

		private volatile String myUsername;
		private volatile String myPassword;

		// 由继承CredentialsCreator的类调用，设置用户名和密码信息
		synchronized public void setCredentials(String username, String password) {
			myUsername = username;
			myPassword = password;
			release();
		}

		// 调用notifyAll唤醒wait()处，继续往下执行
		synchronized public void release() {
			notifyAll();
		}

		// 创建证书
		public Credentials createCredentials(String scheme, AuthScope scope,
				boolean quietly) {
			final String authScheme = scope.getScheme();
			if (!"basic".equalsIgnoreCase(authScheme)
					&& !"digest".equalsIgnoreCase(authScheme)) {
				return null;
			}

			final AuthScopeKey key = new AuthScopeKey(scope);
			Credentials creds = myCredentialsMap.get(key);
			if (creds != null || quietly) {
				return creds;
			}

			final String host = scope.getHost();
			final String area = scope.getRealm();
			// 获取对应的用户名
			final ZLStringOption usernameOption = new ZLStringOption(
					"username", host + ":" + area, "");
			if (!quietly) {
				// 弹出对话框，等待完成输入
				startAuthenticationDialog(host, area, scheme,
						usernameOption.getValue());
				synchronized (this) {
					try {
						// 等待输入完成
						wait();
					} catch (InterruptedException e) {
					}
				}
			}

			if (myUsername != null && myPassword != null) {
				usernameOption.setValue(myUsername);
				creds = new UsernamePasswordCredentials(myUsername, myPassword);
				myCredentialsMap.put(key, creds);
			}
			myUsername = null;
			myPassword = null;
			return creds;
		}

		// 移除证书
		public boolean removeCredentials(AuthScopeKey key) {
			return myCredentialsMap.remove(key) != null;
		}

		// 弹出授权对话框
		abstract protected void startAuthenticationDialog(String host,
				String area, String scheme, String username);
	}

	/**
	 * 身份认证接口
	 * 
	 * @author chenjl
	 * 
	 */
	static interface BearerAuthenticator {
		/**
		 * 认证
		 * 
		 * @param uri
		 * @param realm
		 * @param params
		 * @return
		 */
		Map<String, String> authenticate(URI uri, String realm,
				Map<String, String> params);

		/**
		 * 获取账户名
		 * 
		 * @param host
		 * @param realm
		 *            领域
		 * @return
		 */
		String getAccountName(String host, String realm);

		/**
		 * 设置账户名
		 * 
		 * @param host
		 * @param realm
		 * @param accountName
		 */
		void setAccountName(String host, String realm, String accountName);
	}

	/**
	 * 证书创造器
	 */
	volatile CredentialsCreator myCredentialsCreator;

	/**
	 * 证书提供器
	 * 
	 * @author chenjl
	 * 
	 */
	private class MyCredentialsProvider extends BasicCredentialsProvider {

		private final HttpUriRequest myRequest;
		private final boolean myQuietly;

		MyCredentialsProvider(HttpUriRequest request, boolean quietly) {
			myRequest = request;
			myQuietly = quietly;
		}

		@Override
		public Credentials getCredentials(AuthScope authscope) {
			final Credentials c = super.getCredentials(authscope);
			if (c != null) {
				return c;
			}
			if (myCredentialsCreator != null) {
				return myCredentialsCreator.createCredentials(myRequest
						.getURI().getScheme(), authscope, myQuietly);
			}
			return null;
		}
	};

	/**
	 * 与Cookie相关联的Key
	 * 
	 * @author chenjl
	 * 
	 */
	private static class Key {

		final String Domain;
		final String Path;
		final String Name;

		Key(Cookie c) {
			Domain = c.getDomain();
			Path = c.getPath();
			Name = c.getName();
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof Key)) {
				return false;
			}
			final Key k = (Key) o;
			return MiscUtil.equals(Domain, k.Domain)
					&& MiscUtil.equals(Path, k.Path)
					&& MiscUtil.equals(Name, k.Name);
		}

		@Override
		public int hashCode() {
			return MiscUtil.hashCode(Domain) + MiscUtil.hashCode(Path)
					+ MiscUtil.hashCode(Name);
		}
	};

	/**
	 * Cookie存储
	 */
	final CookieStore CookieStore = new CookieStore() {
		private volatile Map<Key, Cookie> myCookies;

		/**
		 * 存储Cookie
		 */
		public synchronized void addCookie(Cookie cookie) {
			if (myCookies == null) {
				getCookies();
			}
			myCookies.put(new Key(cookie), cookie);
			// 存储到数据库
			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.saveCookies(Collections.singletonList(cookie));
			}
		}

		/**
		 * 清除所有Cookie
		 */
		public synchronized void clear() {
			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.removeAll();
			}
			if (myCookies != null) {
				myCookies.clear();
			}
		}

		/**
		 * 清除过期的Cookie
		 */
		public synchronized boolean clearExpired(Date date) {
			myCookies = null;

			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.removeObsolete(date);
				// TODO: detect if any Cookie has been removed
				return true;
			}
			return false;
		}

		/**
		 * 清除某域
		 */
		public synchronized void clearDomain(String domain) {
			myCookies = null;

			final CookieDatabase db = CookieDatabase.getInstance();
			if (db != null) {
				db.removeForDomain(domain);
			}
		}

		/**
		 * 重置
		 */
		public synchronized void reset() {
			myCookies = null;
		}

		/**
		 * 从数据库中加载所有的Cookie
		 */
		public synchronized List<Cookie> getCookies() {
			if (myCookies == null) {
				myCookies = Collections
						.synchronizedMap(new HashMap<Key, Cookie>());
				final CookieDatabase db = CookieDatabase.getInstance();
				if (db != null) {
					for (Cookie c : db.loadCookies()) {
						myCookies.put(new Key(c), c);
					}
				}
			}
			return new ArrayList<Cookie>(myCookies.values());
		}
	};

	/*
	 * private void setCommonHTTPOptions(HttpMessage request) throws
	 * ZLNetworkException { httpConnection.setInstanceFollowRedirects(true);
	 * httpConnection.setAllowUserInteraction(true); }
	 */

	public void setCredentialsCreator(CredentialsCreator creator) {
		myCredentialsCreator = creator;
	}

	public CredentialsCreator getCredentialsCreator() {
		return myCredentialsCreator;
	}

	/**
	 * 执行网络请求
	 * 
	 * @param request
	 *            请求
	 * @param authenticator
	 *            身份认证接口
	 * @param socketTimeout
	 * @param connectionTimeout
	 * @throws ZLNetworkException
	 */
	void perform(ZLNetworkRequest request, BearerAuthenticator authenticator,
			int socketTimeout, int connectionTimeout) throws ZLNetworkException {

		boolean success = false;// 执行结果
		DefaultHttpClient httpClient = null;// 默认的HttpClient请求类
		HttpEntity entity = null;// 代表返回的网络数据的实体

		try {
			// 配置网络请求参数
			final HttpContext httpContext = new BasicHttpContext();
			httpContext.setAttribute(ClientContext.COOKIE_STORE, CookieStore);

			request.doBefore();
			final HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setSoTimeout(params, socketTimeout);
			HttpConnectionParams
					.setConnectionTimeout(params, connectionTimeout);
			// 构建HttpClient
			httpClient = new DefaultHttpClient(params) {

				// 鉴权处理
				protected AuthenticationHandler createTargetAuthenticationHandler() {

					final AuthenticationHandler base = super
							.createTargetAuthenticationHandler();

					return new AuthenticationHandler() {
						public Map<String, Header> getChallenges(
								HttpResponse response, HttpContext context)
								throws MalformedChallengeException {
							return base.getChallenges(response, context);
						}

						public boolean isAuthenticationRequested(
								HttpResponse response, HttpContext context) {
							return base.isAuthenticationRequested(response,
									context);
						}

						public AuthScheme selectScheme(
								Map<String, Header> challenges,
								HttpResponse response, HttpContext context)
								throws AuthenticationException {
							try {
								return base.selectScheme(challenges, response,
										context);
							} catch (AuthenticationException e) {
								final Header bearerHeader = challenges
										.get("bearer");
								if (bearerHeader != null) {
									String realm = null;
									for (HeaderElement elt : bearerHeader
											.getElements()) {
										final String name = elt.getName();
										if (name == null) {
											continue;
										}
										if ("realm".equals(name)
												|| name.endsWith(" realm")) {
											realm = elt.getValue();
											break;
										}
									}
									throw new BearerAuthenticationException(
											realm, response.getEntity());
								}
								throw e;
							}
						}
					};
				}
			};

			// 请求
			final HttpRequestBase httpRequest;

			if (request instanceof ZLNetworkRequest.Get) {
				// GET请求直接使用HttpGet，传入地址即可
				httpRequest = new HttpGet(request.URL);
			} else if (request instanceof ZLNetworkRequest.PostWithBody) {
				// POST请求
				httpRequest = new HttpPost(request.URL);
				// 额外设置下要POST的Body实体
				((HttpPost) httpRequest)
						.setEntity(new StringEntity(
								((ZLNetworkRequest.PostWithBody) request).Body,
								"utf-8"));
				/*
				 * httpConnection.setRequestProperty( "Content-Length",
				 * Integer.toString(request.Body.getBytes().length) );
				 */
			} else if (request instanceof ZLNetworkRequest.PostWithMap) {
				// POST请求
				final Map<String, String> parameters = ((ZLNetworkRequest.PostWithMap) request).PostParameters;
				httpRequest = new HttpPost(request.URL);
				// 需要将所有的POST参数组装一下
				final List<BasicNameValuePair> list = new ArrayList<BasicNameValuePair>(
						parameters.size());
				for (Map.Entry<String, String> entry : parameters.entrySet()) {
					list.add(new BasicNameValuePair(entry.getKey(), entry
							.getValue()));
				}
				((HttpPost) httpRequest).setEntity(new UrlEncodedFormEntity(
						list, "utf-8"));
			} else if (request instanceof ZLNetworkRequest.FileUpload) {
				// final ZLNetworkRequest.FileUpload uploadRequest =
				// (ZLNetworkRequest.FileUpload) request;
				// POST请求(文件上传)
				httpRequest = new HttpPost(request.URL);
				// 组装参数
				final File file = ((ZLNetworkRequest.FileUpload) request).File;
				final MultipartEntity data = new MultipartEntity(
						HttpMultipartMode.BROWSER_COMPATIBLE, null,
						Charset.forName("utf-8"));
				data.addPart("file", new FileBody(file));
				((HttpPost) httpRequest).setEntity(data);
			} else {
				throw new ZLNetworkException("Unknown request type");
			}

			// 配置公共头信息
			httpRequest.setHeader("User-Agent", ZLNetworkUtil.getUserAgent());
			if (!request.isQuiet()) {
				httpRequest.setHeader("X-Accept-Auto-Login", "True");
			}
			httpRequest.setHeader("Accept-Encoding", "gzip");
			httpRequest.setHeader("Accept-Language", ZLResource.getLanguage());
			// 额外的头信息
			for (Map.Entry<String, String> header : request.Headers.entrySet()) {
				httpRequest.setHeader(header.getKey(), header.getValue());
			}

			// 配置证书提供器
			httpClient.setCredentialsProvider(new MyCredentialsProvider(
					httpRequest, request.isQuiet()));
			// Finally 执行请求
			final HttpResponse response = execute(httpClient, httpRequest,
					httpContext, authenticator);
			// 获取返回的实体内容
			entity = response.getEntity();
			// 如果是遇到了认证问题，并且结果依然是未认证，那么将实体置空null
			if (response.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_UNAUTHORIZED) {
				final AuthState state = (AuthState) httpContext
						.getAttribute(ClientContext.TARGET_AUTH_STATE);
				if (state != null) {
					final AuthScopeKey key = new AuthScopeKey(
							state.getAuthScope());
					if (myCredentialsCreator.removeCredentials(key)) {
						entity = null;
					}
				}
			}
			// 返回码
			final int responseCode = response.getStatusLine().getStatusCode();
			// 返回流
			InputStream stream = null;
			// 实体不为空，并且返回码为200或206
			if (entity != null
					&& (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_PARTIAL)) {
				stream = entity.getContent();
			}

			if (stream != null) {
				try {
					// 返回流编码格式
					final Header encoding = entity.getContentEncoding();
					if (encoding != null
							&& "gzip".equalsIgnoreCase(encoding.getValue())) {
						// GZIP格式流
						stream = new GZIPInputStream(stream);
					}
					// 处理流数据
					request.handleStream(stream,
							(int) entity.getContentLength());
				} finally {
					// 最终要记得关闭
					stream.close();
				}
				success = true;
			} else {
				if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
					throw new ZLNetworkAuthenticationException();
				} else {
					throw new ZLNetworkException(response.getStatusLine()
							.toString());
				}
			}
		} catch (ZLNetworkException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
			final String code;
			if (e instanceof UnknownHostException) {
				code = ZLNetworkException.ERROR_RESOLVE_HOST;
			} else {
				code = ZLNetworkException.ERROR_CONNECT_TO_HOST;
			}
			throw ZLNetworkException.forCode(code,
					ZLNetworkUtil.hostFromUrl(request.URL), e);
		} catch (Exception e) {
			e.printStackTrace();
			throw new ZLNetworkException(e.getMessage(), e);
		} finally {
			request.doAfter(success);
			if (httpClient != null) {
				httpClient.getConnectionManager().shutdown();
			}
			if (entity != null) {
				try {
					entity.consumeContent();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * 执行请求
	 * 
	 * @param client
	 * @param request
	 * @param context
	 * @param authenticator
	 * @return
	 * @throws IOException
	 * @throws ZLNetworkException
	 */
	private HttpResponse execute(DefaultHttpClient client,
			HttpRequestBase request, HttpContext context,
			BearerAuthenticator authenticator) throws IOException,
			ZLNetworkException {
		try {
			return client.execute(request, context);
		} catch (BearerAuthenticationException e) {
			// 认证问题在这里处理
			final Map<String, String> response = authenticator.authenticate(
					request.getURI(), e.Realm, e.Params);
			final String error = response.get("error");
			if (error != null) {
				throw new ZLNetworkAuthenticationException(error, e);
			}
			authenticator.setAccountName(request.getURI().getHost(), e.Realm,
					response.get("user"));
			return client.execute(request, context);
		}
	}
}
