package com.cnblogs.android.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.cnblogs.android.MainActivity;
import com.cnblogs.android.R;
import com.cnblogs.android.utility.FileAccess;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

public class UpdateService extends Service {
	public static final int INIT_UPDATE_NOTIFY = 10010;
	Resources res;
	/**
	 * 下载apk文件，下载完成后提示安装
	 * 
	 */
	class DownloadUpdateFilesTask extends AsyncTask<String, Integer, File> {

		@Override
		protected File doInBackground(String... params) {
			try {
				URL url = new URL(params[0]);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				int length = (int) conn.getContentLength();
				InputStream is = conn.getInputStream();

				if (length != -1) {
					String apkPath = res
							.getString(R.string.app_apk_location_path);

					FileAccess.MakeDir(apkPath);// 创建文件夹
					FileOutputStream out = new FileOutputStream(
							res.getString(R.string.app_update_location_url));
					byte[] buffer = new byte[1024];
					int readLen = 0;
					int destPos = 0;
					int currentPercent = 0;
					while ((readLen = is.read(buffer)) != -1) {
						out.write(buffer, 0, readLen);
						destPos += readLen;
						int p = destPos * 100 / length;
						if (p % 10 == 0 && p != currentPercent) {
							currentPercent = p;
							publishProgress(p);
						}
					}
					out.flush();
					out.close();
				}
				is.close();
				return new File(res.getString(R.string.app_update_location_url));// 生成本地文件名
			} catch (Exception e) {
				e.printStackTrace();
				Toast.makeText(getApplicationContext(), R.string.sys_error,
						Toast.LENGTH_LONG);
			}
			return null;
		}

		@Override
		protected void onPostExecute(File result) {
			if (!result.exists()) {
				Toast.makeText(UpdateService.this, "update failed", 1000)
						.show();
				return;
			}
			Uri uri = Uri.fromFile(result);
			Intent installIntent = new Intent(Intent.ACTION_VIEW);
			installIntent.setDataAndType(uri,
					"application/vnd.android.package-archive");
			installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			long al[] = {80L, 80L};
			updateNotify.vibrate = al;
			updateNotify.icon = android.R.drawable.stat_sys_download_done;
			updateNotify.flags = Notification.FLAG_AUTO_CANCEL;
			updateNotify.defaults = Notification.DEFAULT_SOUND;// 铃声提醒
			updateNotify.contentIntent = PendingIntent.getActivity(
					UpdateService.this, 0, installIntent, 0);// 安装界面
			updateNotify.contentView.setViewVisibility(R.id.progressBlock,
					View.GONE);
			updateNotify.contentView.setViewVisibility(
					R.id.notify_download_done, View.VISIBLE);
			updateNotifMg.notify(INIT_UPDATE_NOTIFY, updateNotify);

			UpdateService.this.stopSelf();
		}

		@Override
		protected void onPreExecute() {
			updateNotify.contentView.setProgressBar(R.id.notify_ProgressBar,
					100, 0, false);
			updateNotify.contentView.setTextViewText(R.id.text_percent, "0%");
			updateNotifMg.notify(INIT_UPDATE_NOTIFY, updateNotify);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			updateNotify.contentView.setProgressBar(R.id.notify_ProgressBar,
					100, values[0], false);
			updateNotify.contentView.setTextViewText(R.id.text_percent,
					values[0] + "%");
			updateNotifMg.notify(INIT_UPDATE_NOTIFY, updateNotify);
		}
	}

	/**
	 * 检测更新条件是否满足，若满足则开始下载程序
	 */
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (!android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment
				.getExternalStorageState())) {
			Toast.makeText(UpdateService.this, R.string.sdcardUnavailable, 1000)
					.show();
			stopSelf();
			return;
		}
		String apkUrl = intent.getStringExtra("apkUrl");
		if (apkUrl.equals("")) {
			Toast.makeText(UpdateService.this, R.string.urlUnavailable, 1000)
					.show();
			stopSelf();
			return;
		} else {
			new DownloadUpdateFilesTask().execute(apkUrl);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/**
	 * 设置通知栏显示
	 */
	@Override
	public void onCreate() {
		updateNotifMg = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		updateNotify = new Notification();
		updateNotify.contentView = new RemoteViews(getPackageName(),
				R.layout.notification);
		updateNotify.contentView.setViewVisibility(R.id.notify_download_done,
				View.GONE);
		updateNotify.icon = android.R.drawable.stat_sys_download;

		Intent i = new Intent(this, MainActivity.class);
		i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		PendingIntent pendingIntent = PendingIntent.getActivity(
				UpdateService.this, 0, i, 0);
		updateNotify.contentIntent = pendingIntent;
		updateNotifMg.notify(INIT_UPDATE_NOTIFY, updateNotify);
		super.onCreate();

		res = this.getResources();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	// 通知栏
	private NotificationManager updateNotifMg;
	private Notification updateNotify;
}
