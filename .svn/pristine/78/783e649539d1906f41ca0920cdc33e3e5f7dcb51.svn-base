package com.sina.book.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarUtil {

	public static long parseFormatDateToTime(String str) {
		// "2013-12-20 11:25:31";
		long millionSeconds = 0l;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			millionSeconds = sdf.parse(str).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return millionSeconds;
	}
	
	// yyyy-MM-dd
	public static String getCurrentTImeWithFormat(String format){
		String curDate = new SimpleDateFormat(format, 
				Locale.getDefault()).format(new Date());
		return curDate;
	}
	

}
