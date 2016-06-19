package com.nfc.project;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class GetCurrentDate {

	public static String getDate(){
		
		String timeStamp = new SimpleDateFormat("yyyy-MM-dd,HH:mm").format(Calendar.getInstance().getTime());
		timeStamp = "2016-06-06,19:05";
		
		return timeStamp;
	}
	
	public static int getDayOfWeek(){
		int day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		day = 2;
		
		return day;
	}
	
	
}
