package com.NewCenturyHotels.NewCentury.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat YYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat MMDD = new SimpleDateFormat("MM-dd");
    public static SimpleDateFormat HHMM = new SimpleDateFormat("HH:mm");

    public Date parseDate(String dateStr){
        try {
            Date time = sdf.parse(dateStr);
            return  time;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Date parseDate(String dateStr,SimpleDateFormat simpleDateFormat){
        try {
            Date time = simpleDateFormat.parse(dateStr);
            return  time;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getDateStr(Date date,SimpleDateFormat sdf){
        String string = sdf.format(date);
        return string;
    }

    public long getTimeSpan(String timeStr){
        long timeSpan = 0;
        try {
            Date time = sdf.parse(timeStr);
            Date now = new Date();
            timeSpan = now.getTime() - time.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeSpan;
    }

    public long getTimeSpan(String time1Str,String time2Str,SimpleDateFormat sdf){
        long timeSpan = 0;
        try {
            Date time1 = sdf.parse(time1Str);
            Date time2 = sdf.parse(time2Str);
            timeSpan = time1.getTime() - time2.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timeSpan;
    }

    public long getDaySpan(long timeSpan){
        timeSpan = timeSpan / 1000 / 3600 /24;
        if(timeSpan >= 1){
            return timeSpan;
        } else {
            return 0;
        }
    }
}
