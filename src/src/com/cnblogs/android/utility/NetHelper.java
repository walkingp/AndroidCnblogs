package com.cnblogs.android.utility;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;

public class NetHelper {
	/**
	 * 获取DefaultHttpClient实例
	 * 
	 * @param charset
	 *            参数编码集, 可空
	 * @return DefaultHttpClient 对象
	 */
	private static DefaultHttpClient getDefaultHttpClient(final String charset) {
		HttpParams httpParams = new BasicHttpParams();

		// 设置连接超时和 Socket 超时，以及 Socket 缓存大小
		HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);

		// 设置重定向，缺省为 true
		HttpClientParams.setRedirecting(httpParams, true);

		// 设置 user agent
		String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
		HttpProtocolParams.setUserAgent(httpParams, userAgent);

		return new DefaultHttpClient(httpParams);
	}
	public static String getData(String url, String charset) {
		if (url == null || "".equals(url)) {
			return null;
		}
		String responseStr = "";
		HttpClient httpClient = null;
		HttpGet hg = null;
		try {
			httpClient = getDefaultHttpClient(charset);
			hg = new HttpGet(url);
			// 发送请求，得到响应
			HttpResponse response = httpClient.execute(hg);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				return "";
			}
			responseStr = EntityUtils.toString(response.getEntity(), charset);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		} finally {
			if (httpClient != null) {
				try {
					httpClient.getConnectionManager().shutdown();
				} catch (Exception e) {
				}
			}
		}
		return responseStr;
	}
	/**
	 * 获取网络是否可用状态
	 * 
	 * @return
	 */
	public static boolean networkIsAvailable(Context context) {
		ConnectivityManager cManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cManager.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		if (info.isConnected()) {
			return true;
		}
		return false;
	}
	/**
	 * 读取网络数据
	 * 
	 * @param _url
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String GetContentFromUrl(String url) {
		String result = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpUriRequest req = new HttpGet(url);
			HttpResponse resp = client.execute(req);
			HttpEntity ent = resp.getEntity();
			int status = resp.getStatusLine().getStatusCode();
			// If the status is equal to 200 ，that is OK
			if (status == HttpStatus.SC_OK) {
				result = EntityUtils.toString(ent);
				// Encode utf-8 to iso-8859-1
				// result = new String(result.getBytes("ISO-8859-1"), "UTF-8");
			}
			client.getConnectionManager().shutdown();
			return result;
		} catch (Exception e) {
			Log.e("NetHelper", "______________读取数据失败" + e.toString()
					+ "_____________");
			return "";
		}
	}
	/**
	 * 得到xml内容
	 * 
	 * @param url
	 * @param contentType
	 * @return
	 */
	public static String GetXmlContentFromUrl(String url, String contentType) {
		return GetContentFromUrl(url, contentType).replaceAll("\n|\t|\r", "");
	}
	/**
	 * 读取网络数据
	 * 
	 * @param _url
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public static String GetContentFromUrl(String url, String contentType) {
		String result = "";
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpUriRequest req = new HttpGet(url);
			HttpResponse resp = client.execute(req);
			req.getParams().setParameter("Content-Type", "UTF-8");
			HttpEntity ent = resp.getEntity();
			int status = resp.getStatusLine().getStatusCode();
			// If the status is equal to 200 ，that is OK
			if (status == HttpStatus.SC_OK) {
				result = EntityUtils.toString(ent);
				// Encode utf-8 to iso-8859-1
				result = new String(result.getBytes("ISO-8859-1"), contentType);
			}
			client.getConnectionManager().shutdown();
			return result;
		} catch (Exception e) {
			Log.e("NetHelper", "______________读取数据失败" + e.toString()
					+ "_____________");
			return "";
		}
	}
	/**
	 * 带参数Post数据获得返回
	 * 
	 * @param url
	 * @param params
	 * @return
	 */
	public static String GetContentFromUrlByPostParams(String url,
			List<NameValuePair> params) {
		DefaultHttpClient httpClient = new DefaultHttpClient();

		try {
			HttpPost httpPost = new HttpPost(url);
			HttpEntity postEntity = new UrlEncodedFormEntity(params);
			httpPost.setEntity(postEntity);
			HttpResponse httpResponse = httpClient.execute(httpPost);
			int responseCode = httpResponse.getStatusLine().getStatusCode();
			if (responseCode == 200) {
				String result = httpResponse.getEntity().toString();
				return result;
			}
			if (responseCode == 403) {
				return "1";// 已经关注了此人
			}
		} catch (Exception e) {
			Log.e("NetHelper", "______________读取数据失败" + e.toString()
					+ "_____________");
			e.printStackTrace();
		}
		httpClient.getConnectionManager().shutdown();
		return "";
	}
	/**
	 * 读取输入流
	 */
	public static byte[] readInputStream(InputStream inStream) throws Exception {
		ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len = 0;
		while ((len = inStream.read(buffer)) != -1) {
			outSteam.write(buffer, 0, len);
		}
		outSteam.close();
		inStream.close();
		return outSteam.toByteArray();
	}
	/**
	 * 下载图片到本地
	 * 
	 * @param url
	 * @return
	 */
	public static Drawable loadImageFromUrlWithStore(String folder, String url) {
		try {
			//注意url可能包含?的情况，需要在?前截断
			if(url.indexOf("?")>0){
				url=url.substring(0,url.indexOf("?"));
			}
			String fileName = url.substring(url.lastIndexOf("/") + 1);
			String encodeFileName = URLEncoder.encode(fileName);
			URL imageUrl = new URL(url.replace(fileName, encodeFileName));
			byte[] data = readInputStream((InputStream) imageUrl.openStream());
			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			String status = Environment.getExternalStorageState();
			if (status.equals(Environment.MEDIA_MOUNTED)) {
				FileAccess.MakeDir(folder);
				String outFilename = folder + fileName;
				bitmap.compress(CompressFormat.PNG, 100, new FileOutputStream(
						outFilename));
				Bitmap bitmapCompress = BitmapFactory.decodeFile(outFilename);
				Drawable drawable = new BitmapDrawable(bitmapCompress);
				return drawable;
			}
		} catch (Exception e) {
			Log.e("download_img_err", e.toString());
		}
		return null;
	}
	/**
	 * 下载图片
	 * 
	 * @param url
	 * @return
	 */
	public static Drawable loadImageFromUrl(String url) {
		InputStream is = null;
		try {
			String fileName = url.substring(url.lastIndexOf("/") + 1);
			String encodeFileName = URLEncoder.encode(fileName);
			URL imageUrl = new URL(url.replace(fileName, encodeFileName));
			is = (InputStream) imageUrl.getContent();
		} catch (Exception e) {
			Log.e("There", e.toString());
		}
		Drawable d = Drawable.createFromStream(is, "src");
		return d;
	}
}
