package com.cnblogs.android.cache;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import com.cnblogs.android.R;
import com.cnblogs.android.utility.NetHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class AsyncImageLoader {
	private HashMap<String, SoftReference<Drawable>> imageCache;
	Context curContext;
	public AsyncImageLoader(Context context) {
		curContext = context;
		imageCache = new HashMap<String, SoftReference<Drawable>>();
	}
	/*
	 * 直接下载图片
	 */
	public void loadDrawable(final ImageCacher.EnumImageType imgType,
			final String imageUrl) {
		final String folder = ImageCacher.GetImageFolder(imgType);

		new Thread() {
			public void run() {
				NetHelper.loadImageFromUrlWithStore(folder, imageUrl);
			}
		}.start();
	}
	/**
	 * 将下载到本地并保存
	 * 
	 * @param imgType
	 * @param tag
	 * @param imageCallback
	 * @return
	 */
	public Drawable loadDrawable(final ImageCacher.EnumImageType imgType,
			final String tag, final ImageCallback imageCallback) {
		Drawable sampleDrawable = curContext.getResources().getDrawable(
				R.drawable.sample_face);
		if (tag.trim().equals("")) {
			return sampleDrawable;
		}
		String[] twoParts = tag.split("\\|", 2);
		final String imageUrl = twoParts[0];
		final String folder = ImageCacher.GetImageFolder(imgType);
		String outFilename = folder
				+ imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		Log.i("下载", tag);
		Log.i("本地", outFilename);
		File file = new File(outFilename);
		if (imageCache.containsKey(imageUrl)) {
			SoftReference<Drawable> softReference = imageCache.get(imageUrl);
			Drawable drawable = softReference.get();
			if (drawable != null) {
				return drawable;
			}
		} else if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(outFilename);
			Drawable drawable = new BitmapDrawable(bitmap);
			return drawable;
		}

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {
				imageCallback.imageLoaded((Drawable) message.obj, tag);
			}
		};

		new Thread() {
			public void run() {
				Drawable drawable = NetHelper.loadImageFromUrlWithStore(folder,
						imageUrl);
				if (drawable == null) {
					drawable = NetHelper.loadImageFromUrl(imageUrl);
					if (drawable != null) {
						imageCache.put(imageUrl, new SoftReference<Drawable>(
								drawable));
					}
				} else
					imageCache.put(imageUrl, new SoftReference<Drawable>(
							drawable));
				Message message = handler.obtainMessage(0, drawable);
				handler.sendMessage(message);
			}
		}.start();
		return sampleDrawable;
	}

	public interface ImageCallback {
		public void imageLoaded(Drawable imageDrawable, String tag);
	}

}
