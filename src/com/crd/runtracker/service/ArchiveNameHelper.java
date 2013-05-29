package com.crd.runtracker.service;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.crd.runtracker.dao.Archive;

public class ArchiveNameHelper {
	private Context context;
	private SharedPreferences sharedPreferences;
	private static final String LAST_OPENED_ARCHIVE_FILE_NAME = "lastOpenedArchiveFileName";
	private SharedPreferences.Editor editor;

	public static final String SQLITE_DATABASE_FILENAME_EXT = ".sqlite";
	public static final String SAVED_EXTERNAL_DIRECTORY = "gpstracker";
	public static final String GROUP_BY_EACH_MONTH = "yyyyMM";
	public static final String GROUP_BY_EACH_DAY = "yyyyMMdd";

	public ArchiveNameHelper(Context context) {
		this.context = context;
		this.sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		this.editor = sharedPreferences.edit();
	}

	public static boolean isExternalStoragePresent() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}

	public static File getExternalStoragePath() {
		if (isExternalStoragePresent()) {
			return Environment.getExternalStorageDirectory();
		}
		return null;
	}

	public static File getStorageDirectory(Date date) {
		String saveDirectory = getExternalStoragePath() + File.separator
				+ SAVED_EXTERNAL_DIRECTORY + File.separator
				+ new SimpleDateFormat(GROUP_BY_EACH_MONTH).format(date);

		// 如果保存目录不存在，则自动创建个
		File saveDirectoryFile = new File(saveDirectory);
		if (!saveDirectoryFile.isDirectory()) {
			saveDirectoryFile.mkdirs();
		}

		return saveDirectoryFile;
	}

	public static File getCurrentStorageDirectory() {
		return getStorageDirectory(new Date());
	}

	public ArrayList<String> getArchiveFilesNameByMonth(Date date) {
		ArrayList<String> result = new ArrayList<String>();

		File storageDirectory = getStorageDirectory(date);
		File[] archiveFiles = storageDirectory.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File file, String s) {
				return s.endsWith(SQLITE_DATABASE_FILENAME_EXT);
			}
		});

		if (archiveFiles != null) {
			// 根据最后修改时间排序
			Arrays.sort(archiveFiles, new Comparator<File>() {
				@Override
				public int compare(File f1, File f2) {
					try {
						Archive archive1 = new Archive(context, f1.getAbsolutePath(), Archive.MODE_READ_ONLY);
						Archive archive2 = new Archive(context, f2.getAbsolutePath(), Archive.MODE_READ_ONLY);
						
						Location location1 = archive1.getFirstRecord();
						Location location2 = archive2.getFirstRecord();
						
						Long time1 = location1.getTime();
						Long time2 = location2.getTime();
						
						archive1.close();
						archive2.close();
						
						return Long.valueOf(time2).compareTo(time1);
					} catch (NullPointerException e) {
						return 0;
					}
					
				}
			});

			for (int i = 0; i < archiveFiles.length; i++) {
				result.add((archiveFiles[i]).getAbsolutePath());
			}
		}

		return result;
	}

	public ArrayList<String> getArchiveFilesFormCurrentMonth() {
		return getArchiveFilesNameByMonth(new Date());
	}

	public String getNewName() {
		String databaseFileName = System.currentTimeMillis()
				+ SQLITE_DATABASE_FILENAME_EXT;

		File databaseFile = new File(getCurrentStorageDirectory()
				.getAbsolutePath() + File.separator + databaseFileName);
		return databaseFile.getAbsolutePath();
	}

	/**
	 * 获得已经存在过的未清理的文件
	 * 
	 * @return
	 */
	public String getResumeName() {
		if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
			return sharedPreferences.getString(LAST_OPENED_ARCHIVE_FILE_NAME,
					"");
		}

		return null;
	}

	public boolean clearLastOpenedName() {
		if (sharedPreferences.contains(LAST_OPENED_ARCHIVE_FILE_NAME)) {
			editor.remove(LAST_OPENED_ARCHIVE_FILE_NAME);
			return editor.commit();
		}

		return false;
	}

	public boolean setLastOpenedName(String name) {
		editor.putString(LAST_OPENED_ARCHIVE_FILE_NAME, name);
		return editor.commit();
	}

	public boolean hasResumeName() {
		String resumeArchiveFileName = getResumeName();
		if (resumeArchiveFileName != null) {
			File resumeFile = new File(resumeArchiveFileName);
			return (resumeFile.exists() && resumeFile.isFile() && resumeFile
					.canWrite()) ? true : false;
		} else {
			return false;
		}
	}
}
