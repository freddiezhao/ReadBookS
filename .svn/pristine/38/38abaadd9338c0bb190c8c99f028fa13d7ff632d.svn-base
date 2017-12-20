package com.sina.book.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.Proxy;
import android.net.Uri;
import android.text.TextUtils;

import com.sina.book.SinaBookApplication;
import com.sina.book.data.ConstantData;
import com.sina.book.useraction.DeviceInfo;

/**
 * http请求相关的工具方法
 * 
 * @author Tsmile
 * 
 */
public class HttpUtil {

	private static final String TAG = "HttpUtil";

	private static final int TIME_OUT_CONNECTION = 50000;
	private static final int TIME_OUT_SOCKET = 20000;
	private static final int BUFFER_SIZE = 1024 * 1024;

	public static final String BOUNDARY = "7cd4a6d158c";
	public static final String MP_BOUNDARY = "--" + BOUNDARY;
	public static final String END_MP_BOUNDARY = "--" + BOUNDARY + "--";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";

	/**
	 * 每个请求都需要加AUTHCODE
	 * 
	 * @param url
	 * @return
	 */
	public static String addAuthCode2Url(String url) {
		// StringBuilder sb = new StringBuilder(url);
		// if (!url.contains("?")) {
		// sb.append("?");
		// } else {
		// sb.append("&");
		// }
		// sb.append(ConstantData.AUTH_CODE_GET);
		// sb.append("&");
		// sb.append(ConstantData.FROM_CLIENT);
		// sb.append("&app_channel=");
		// sb.append(ConstantData.getChannelCode(SinaBookApplication.gContext));
		// if (!url.contains("version")) {
		// sb.append("&version=");
		// sb.append(ConstantData.VERSION);
		// }
		// return sb.toString();
		// 上面的方法可能存在同时多个的情况，因此用下面代码替换
		url = setURLParams(url, ConstantData.AUTH_CODE_KEY, ConstantData.AUTH_CODE_VALUE);
		url = setURLParams(url, ConstantData.FROM_CLIENT_KEY, ConstantData.FROM_CLIENT_VALUE);
		// TODO 暂时更改成38，请求渠道包信息
		// url = setURLParams(url, ConstantData.APP_CHANNEL_KEY, "38");
		url = setURLParams(url, ConstantData.APP_CHANNEL_KEY,
				String.valueOf(ConstantData.getChannelCode(SinaBookApplication.gContext)));
		url = setURLParams(url, ConstantData.APP_VERSION_KEY, ConstantData.APP_VERSION_VALUE);
		return url;
	}

	// Add By ChenJianLi
	public static String setURLParams(String url, String name, String params) {
		if (url != null && name != null && params != null) {
			if (url.contains(name)) {
				int index = url.indexOf(name);
				int afterIndex = index + name.length();
				String after = url.substring(afterIndex, afterIndex + 1);
				if (after.equals("=")) {
					int beforeIndex = index - 1;
					String before = url.substring(beforeIndex, index);
					if ("?".equals(before) || "&".equals(before)) {
						int _index = url.indexOf("&", afterIndex + 1);
						if (_index != -1) {
							String temp1 = url.substring(0, afterIndex + 1);
							String temp2 = url.substring(_index);
							url = temp1 + params + temp2;
						} else {
							String temp = url.substring(0, afterIndex + 1);
							url = temp + params;
						}
					}
				}
			} else {
				int index = url.lastIndexOf("?");
				if (index != -1) {
					url += "&";
				} else {
					url += "?";
				}
				url += name + "=" + params;
			}
		}
		return url;
	}

	// Add By ChenJianLi
	public static String getUrlKeyValue(String url, String key) {
		String value = null;
		String tempKey = "?" + key + "=";
		if (url.indexOf(tempKey) == -1) {
			tempKey = "&" + key + "=";
			if (url.indexOf(tempKey) == -1) {
				return null;
			} else {
				value = cumtomerValue(url, tempKey);
			}
		} else {
			value = cumtomerValue(url, tempKey);
		}
		return value;
	}

	// Add By ChenJianLi
	private static String cumtomerValue(String url, String key) {
		String value = null;
		int fromIndex = url.indexOf(key);
		if (fromIndex != -1) {
			int endIndex = url.indexOf("&", fromIndex + 1);
			if (endIndex == -1) {
				value = url.substring(fromIndex + key.length());
				if (value == null || value.length() == 0) {
					return null;
				}
				return value;
			} else {
				value = url.substring(fromIndex + key.length(), endIndex);
				if (value == null || value.length() == 0) {
					return null;
				}
				return value;
			}
		}
		return value;
	}

	public static String inputStream2String(InputStream in) throws IOException {
		if (in == null)
			return "";

		final int size = 128;
		byte[] buffer = new byte[size];

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int cnt = 0;
		while ((cnt = in.read(buffer)) > -1) {
			baos.write(buffer, 0, cnt);
		}
		baos.flush();

		in.close();
		baos.close();

		return baos.toString();
	}

	/**
	 * 获取网络状态
	 * 
	 * @return 网络状态：State.*
	 */
	public static State getConnectionState(Context context) {
		ConnectivityManager sManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = sManager.getActiveNetworkInfo();
		if (info != null) {
			return info.getState();
		}
		return State.UNKNOWN;
	}

	/**
	 * 网络连接是否已连接好
	 * 
	 * @return
	 */
	public static boolean isConnected(Context context) {
		if (context == null) {
			context = SinaBookApplication.gContext;
		}
		return State.CONNECTED.equals(getConnectionState(context));
	}

	/**
	 * 获取httpclient进行网络请求
	 */
	public static HttpClient getHttpClient(Context context) throws IOException {
		NetworkState state = getNetworkState(context);
		HttpClient client = createHttpClient();

		if (state == NetworkState.NOTHING) {
			throw new IOException("NoSignalException");
		} else if (state == NetworkState.MOBILE) {
			APNWrapper wrapper = null;
			wrapper = getAPN(context);
			if (!TextUtils.isEmpty(wrapper.proxy)) {
				client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
						new HttpHost(wrapper.proxy, wrapper.port));
			}
		}

		// HttpConnectionParamBean paramHelper = new
		// HttpConnectionParamBean(client.getParams());
		// paramHelper.setSoTimeout(TIME_OUT_CONNECTION);
		// paramHelper.setConnectionTimeout(TIME_OUT_SOCKET);
		return client;
	}

	/**
	 * 上传普通的键值对
	 * 
	 * @param baos
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	private static void paramToUpload(OutputStream baos, String key, String value) throws IOException {
		StringBuilder temp = new StringBuilder(10);
		temp.setLength(0);
		temp.append(MP_BOUNDARY).append("\r\n");
		temp.append("content-disposition: form-data; name=\"").append(key);
		temp.append("\"\r\n");
		temp.append("Content-Type: text").append("\r\n\r\n");
		temp.append(value).append("\r\n");
		byte[] res = temp.toString().getBytes();
		baos.write(res);
	}

	/**
	 * 上传文件
	 * 
	 * @param out
	 * @param imgpath
	 * @throws IOException
	 */
	private static void imageContentToUpload(OutputStream out, Bitmap imgpath, String bitmapName) throws IOException {
		StringBuilder temp = new StringBuilder();

		temp.append(MP_BOUNDARY).append("\r\n");
		temp.append("Content-Disposition: form-data; name=\"").append(bitmapName).append("\"; filename=\"")
				.append("uploadfile").append("\"\r\n");
		String filetype = "image/jpeg";
		temp.append("Content-Type: ").append(filetype).append("\r\n\r\n");
		byte[] res = temp.toString().getBytes();
		out.write(res);
		BufferedInputStream bis = null;
		try {
			imgpath.compress(CompressFormat.JPEG, 75, out);
			out.write("\r\n".getBytes());
		} catch (IOException e) {
			LogUtil.e(TAG, "upload fail:" + e.toString());
		} finally {
			if (null != bis) {
				try {
					bis.close();
				} catch (IOException e) {
					LogUtil.e(TAG, "upload fail:" + e.toString());
				}
			}
		}

	}

	/**
	 * 上传时的结尾标记
	 * 
	 * @param baos
	 * @throws IOException
	 */
	private static void writeEndToUpload(OutputStream baos) throws IOException {
		baos.write(("\r\n" + END_MP_BOUNDARY).getBytes());
	}

	/**
	 * 使用httpclient进行post请求
	 */
	public static HttpResponse doFilePostRequest(HttpClient client, String url, List<NameValuePair> postParams,
			Bitmap image, String bitmapName) throws IOException {
		HttpPost httpPostRequest = new HttpPost(url);
		client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, 60000);
		httpPostRequest.setHeader("Content-Type", MULTIPART_FORM_DATA + "; boundary=" + BOUNDARY);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(BUFFER_SIZE);
		if (null != postParams) {
			for (int i = 0; i < postParams.size(); i++) {
				NameValuePair param = postParams.get(i);
				paramToUpload(baos, param.getName(), param.getValue());
			}
		}
		try {
			imageContentToUpload(baos, image, bitmapName);
		} catch (FileNotFoundException e) {
			LogUtil.e(TAG, "upload fail:" + e.toString());
		}

		writeEndToUpload(baos);
		byte[] mData = baos.toByteArray();
		baos.close();
		ByteArrayEntity formMultiEntity = new ByteArrayEntity(mData);
		httpPostRequest.setEntity(formMultiEntity);
		return client.execute(httpPostRequest);
	}

	public static HttpResponse doPostRequest(HttpClient client, String url, List<NameValuePair> postParams)
			throws IOException {
		HttpPost httpPostRequest = new HttpPost(url);
		httpPostRequest.setHeader("content-type", "application/x-www-form-urlencoded");
		httpPostRequest.setHeader("charset", "UTF-8");
		if (postParams != null && postParams.size() > 0) {
			StringBuilder sbParam = new StringBuilder();
			for (int i = 0; i < postParams.size(); i++) {
				NameValuePair param = postParams.get(i);
				if (param.getValue() != null) {
					if (i != 0) {
						sbParam.append("&");
					}
					sbParam.append(param.getName()).append("=").append(URLEncoder.encode(param.getValue(), "utf-8"));
				}
			}
			byte[] bytes = sbParam.toString().getBytes("UTF-8");
			ByteArrayEntity formEntity = new ByteArrayEntity(bytes);
			httpPostRequest.setEntity(formEntity);
		}
		return client.execute(httpPostRequest);
	}

	/**
	 * 使用httpclient进行get请求
	 */
	public static HttpResponse doGetRequest(HttpClient client, String url) throws IOException {
		HttpGet httpGetRequest = new HttpGet(url);
		return client.execute(httpGetRequest);
	}
	
	/**
	 * 使用httpclient进行get请求
	 */
	public static HttpResponse doGetRequestWithHasRedirectURL(HttpClient client, String url, HttpContext httpContext) throws IOException {
		HttpGet httpGetRequest = new HttpGet(url);
		return client.execute(httpGetRequest, httpContext);
	}

	public static HttpURLConnection getHttpUrlConnection(URL url, Context context, boolean disableAttr) throws ProtocolException,
			IOException {
		// 统一加上参数
		if (url != null) {
			String netUrl = url.toString();
			// 获取GSID的地址不加统一参数
			if (!netUrl.startsWith("https://m.weibo.cn/login") || !disableAttr) {
				String carrier = DeviceInfo.getCarrier(SinaBookApplication.gContext);
				String apn = HttpUtil.getNetworkType(SinaBookApplication.gContext);
				String imei = ConstantData.getDeviceId();
				String deviceId = DeviceInfo.getUDID();
				String appChannel = String.valueOf(ConstantData.getChannelCode(SinaBookApplication.gContext));
				netUrl = HttpUtil.setURLParams(netUrl, ConstantData.OPERATORS_NAME_KEY, carrier);
				netUrl = HttpUtil.setURLParams(netUrl, ConstantData.APN_ACCESS_KEY, apn);
				netUrl = HttpUtil.setURLParams(netUrl, ConstantData.PHONE_IMEI_KEY, imei);
				netUrl = HttpUtil.setURLParams(netUrl, ConstantData.DEVICE_ID_KEY, deviceId);
				netUrl = HttpUtil.setURLParams(netUrl, ConstantData.APP_CHANNEL_KEY, appChannel);
				netUrl = HttpUtil.setURLParams(netUrl, ConstantData.APP_VERSION_KEY, ConstantData.APP_VERSION_VALUE);
				url = null;
				url = new URL(netUrl);
			}
		}

		// lyang add
		HttpURLConnection httpConnection;
		if (isWapNet(context)) {// wap 网络
			String tempUrl = url.toString();
			int offset = tempUrl.startsWith("https") ? 8 : 7;
			if (offset == 7) {// http开头的
				int contentBeginIdx = tempUrl.indexOf('/', offset);
				StringBuffer urlStringBuffer = new StringBuffer("http://10.0.0.172");
				urlStringBuffer.append(tempUrl.substring(contentBeginIdx));
				URL urltemp = new URL(urlStringBuffer.toString());
				httpConnection = (HttpURLConnection) urltemp.openConnection();
				httpConnection.setRequestProperty("X-Online-Host", tempUrl.substring(offset, contentBeginIdx));
				// Log.e("net ", "wap");
			} else {// wap 网络 访问https
				httpConnection = (HttpURLConnection) url.openConnection();
			}
		} else {
			String[] hostAndPort = getProxyHostAndPort(context);
			String host = hostAndPort[0];
			int port = Integer.parseInt(hostAndPort[1]);

			if (host != null && host.length() != 0 && port != -1) {// 电信wap
																	// 普通移动net网络
				InetSocketAddress isa = new InetSocketAddress(host, port);
				java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, isa);
				httpConnection = (HttpURLConnection) url.openConnection(proxy);
			} else {// wifi 网络
				httpConnection = (HttpURLConnection) url.openConnection();
			}
		}

		httpConnection.setDoInput(true);
		httpConnection.setConnectTimeout(60000);
		httpConnection.setReadTimeout(60000);
		httpConnection.setRequestProperty("Accept", "*, */*");
		httpConnection.setRequestProperty("accept-charset", "utf-8");
		httpConnection.setRequestMethod("GET");
		return httpConnection;
	}

	public enum NetworkState {
		NOTHING, MOBILE, WIFI
	}

	public static NetworkState getNetworkState(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null || !info.isAvailable()) {
			return NetworkState.NOTHING;
		} else {
			if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
				return NetworkState.MOBILE;
			} else {
				return NetworkState.WIFI;
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static String[] getProxyHostAndPort(Context context) {
		if (getNetworkState(context) == NetworkState.WIFI) {
			return new String[] { "", "-1" };
		} else {
			return new String[] { Proxy.getDefaultHost(), "" + Proxy.getDefaultPort() };
		}
	}

	public static String getNetworkType(Context ctx) {
		String defaultType = "none";
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null) {
			int type = networkInfo.getType();
			// State state = networkInfo.getState();
			// TODO 这里我们只是判断接入点情况，不必非得是网络连接良好才返回
			// if (networkInfo.isAvailable() && state == State.CONNECTED) {
			if (type == ConnectivityManager.TYPE_WIFI) {
				return "wifi";
			} else if (type == ConnectivityManager.TYPE_MOBILE) {
				String extraInfo = networkInfo.getExtraInfo();
				if (extraInfo != null)
					return extraInfo;
			}
			// }
		}
		return defaultType;
	}

	public static boolean isWapNet(Context context) {
		String currentAPN = "";
		ConnectivityManager conManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conManager.getActiveNetworkInfo();
		if (info == null || !info.isAvailable()) {
			return false;
		}
		if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			return false;
		}
		currentAPN = info.getExtraInfo();
		if (currentAPN == null || currentAPN.equals("")) {
			return false;
		} else {
			if (currentAPN.equals("cmwap") || currentAPN.equals("uniwap") || currentAPN.equals("3gwap")) {

				return true;
			} else {
				return false;
			}
		}
	}

	public static class APNWrapper {
		public String name;
		public String apn;
		public String proxy;
		public int port;

		public String getApn() {
			return apn;
		}

		public String getName() {
			return name;
		}

		public int getPort() {
			return port;
		}

		public String getProxy() {
			return proxy;
		}

		APNWrapper() {
		}

		public String toString() {
			return "{name=" + name + ";apn=" + apn + ";proxy=" + proxy + ";port=" + port + "}";
		}
	}

	@SuppressWarnings("deprecation")
	public static APNWrapper getAPN(Context ctx) {
		APNWrapper wrapper = new APNWrapper();
		Cursor cursor = null;
		try {
			cursor = ctx.getContentResolver().query(Uri.parse("content://telephony/carriers/preferapn"),
					new String[] { "name", "apn", "proxy", "port" }, null, null, null);
		} catch (Exception e) {
			// 为了解决在4.2系统上禁止非系统进程获取apn相关信息，会抛出安全异常
			// java.lang.SecurityException: No permission to write APN settings
		}
		if (cursor != null) {
			cursor.moveToFirst();
			if (cursor.isAfterLast()) {
				wrapper.name = "N/A";
				wrapper.apn = "N/A";
			} else {
				wrapper.name = cursor.getString(0) == null ? "" : cursor.getString(0).trim();
				wrapper.apn = cursor.getString(1) == null ? "" : cursor.getString(1).trim();
			}
			cursor.close();
		} else {
			wrapper.name = "N/A";
			wrapper.apn = "N/A";
		}
		wrapper.proxy = android.net.Proxy.getDefaultHost();
		wrapper.proxy = TextUtils.isEmpty(wrapper.proxy) ? "" : wrapper.proxy;
		wrapper.port = android.net.Proxy.getDefaultPort();
		wrapper.port = wrapper.port > 0 ? wrapper.port : 80;
		return wrapper;
	}

	protected static DefaultHttpClient createHttpClient() {

		// sets up parameters
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		params.setBooleanParameter("http.protocol.expect-continue", false);
		ConnManagerParams.setTimeout(params, 1000);
		/* 连接超时 */
		HttpConnectionParams.setConnectionTimeout(params, TIME_OUT_CONNECTION);
		/* 请求超时 */
		HttpConnectionParams.setSoTimeout(params, TIME_OUT_SOCKET);

		// registers schemes for both http and https
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		try {

			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);

			EasySSLSocketFactory sf = new EasySSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			registry.register(new Scheme("https", sf, 443));
		} catch (Exception e) {
			// Log.e(TAG, "https:", e);
		}
		ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(params, registry);
		return new DefaultHttpClient(manager, params);
	}

	private static class EasySSLSocketFactory extends SSLSocketFactory {

		protected SSLContext Cur_SSL_Context;

		public EasySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);
			try {
				Cur_SSL_Context = SSLContext.getInstance("TLS");
			} catch (Exception e) {
				Cur_SSL_Context = SSLContext.getInstance("LLS");
			}
			Cur_SSL_Context.init(null, new TrustManager[] { new EasyX509TrustManager() }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException {
			return Cur_SSL_Context.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return Cur_SSL_Context.getSocketFactory().createSocket();
		}

	}

	private static class EasyX509TrustManager implements X509TrustManager {

		public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {

		}

		public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {

		}

		public X509Certificate[] getAcceptedIssuers() {
			return null;
		}

	}
}
