package com.cnblogs.android.utility;

import java.io.File;
import java.text.DecimalFormat;

import com.cnblogs.android.core.Config;
import android.os.Environment;

public class FileAccess {
	/**
	 * 创建文件夹
	 * 
	 * @param dirName
	 */
	public static void MakeDir(String dirName) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File destDir = new File(dirName);
			if (!destDir.exists()) {
				destDir.mkdirs();
			}
		}
	}

	/**
	 * 删除此路径名表示的文件或目录。 如果此路径名表示一个目录，则会先删除目录下的内容再将目录删除，所以该操作不是原子性的。
	 * 如果目录中还有目录，则会引发递归动作。
	 * 
	 * @param filePath
	 *            要删除文件或目录的路径。
	 * @return 当且仅当成功删除文件或目录时，返回 true；否则返回 false。
	 */
	public static boolean DeleteFile(String filePath) {
		File file = new File(filePath);
		if (file.listFiles() == null)
			return true;
		else {
			File[] files = file.listFiles();
			for (File deleteFile : files) {
				if (deleteFile.isDirectory())
					DeleteAllFile(deleteFile);
				else
					deleteFile.delete();
			}
		}
		return true;
	}
	/**
	 * 删除全部文件
	 * 
	 * @param file
	 * @return
	 */
	private static boolean DeleteAllFile(File file) {
		File[] files = file.listFiles();
		for (File deleteFile : files) {
			if (deleteFile.isDirectory()) {
				// 如果是文件夹，则递归删除下面的文件后再删除该文件夹
				if (!DeleteAllFile(deleteFile)) {
					// 如果失败则返回
					return false;
				}
			} else {
				if (!deleteFile.delete()) {
					// 如果失败则返回
					return false;
				}
			}
		}
		return file.delete();
	}
	/**
	 * 得到数据库文件路径
	 * @return
	 */
	public static String GetDbFileAbsolutePath(){
		String dbPath="/data/data/" + Config.APP_PACKAGE_NAME + "/databases/" + Config.DB_FILE_NAME;
		return dbPath;
	}
	/**
	 * 读取文件大小
	 * @param filePath
	 * @return
	 */
	public static long GetFileLength(String filePath){
		File file=new File(filePath);
		return file.length();
	}
	/**
	 * 读取文件夹大小
	 * @param dirPath
	 * @return
	 */
	public static long GetPathLength(String dirPath){
		File dir=new File(dirPath);
		return getDirSize(dir);
	}
	/**
	 * 读取文件夹大小
	 * @param dir
	 * @return
	 */
	private static long getDirSize(File dir) {  
	    if (dir == null) {  
	        return 0;  
	    }  
	    if (!dir.isDirectory()) {  
	        return 0;  
	    }  
	    long dirSize = 0;  
	    File[] files = dir.listFiles();  
	    for (File file : files) {  
	        if (file.isFile()) {  
	            dirSize += file.length();  
	        } else if (file.isDirectory()) {  
	            dirSize += file.length();  
	            dirSize += getDirSize(file); // 如果遇到目录则通过递归调用继续统计  
	        }  
	    }  
	    return dirSize;  
	} 
	/**
	 * 将字长长度转换为KB/MB
	 * @param size
	 * @return
	 */
	public static String GetFileSize(long size){
		int kbSize=(int)size/1024;
		if(kbSize>1024){
			float mbSize=kbSize/1024;
			DecimalFormat formator=new DecimalFormat( "##,###,###.## ");
			return formator.format(mbSize) + "M";
		}
		return kbSize + "K";
	}
}
