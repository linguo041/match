package com.roy.football.match.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtil {
	public final static String simple_date_format = "yyyyMMdd";
	public static final String simple_date_format_dot = "yyyy.MM.dd";
	public static final String simple_date_format_dash = "yyyy-MM-dd";
	public static final String simple_date_format_slash = "yyyy/MM/dd";
	public static final String date_time_format = "yyyy-MM-dd-HH:mm:ss";
	public static final String date_time_complex_format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    /**-- add start ziczhou 2015.06.02 --*/
    /** "yyyy-MM-dd HH:mm:ss" format */
	public static final String DATE_TIME_DATABASE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	
	public static final String EIGHTWIN_FORMAT = "yyyy-MM-dd HH:mm";

	public final static TimeZone GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
	public final static TimeZone GMT_PLUS8_TIMEZONE = TimeZone.getTimeZone("GMT+8");
	public final static TimeZone PST_TIMEZONE = TimeZone.getTimeZone("PST");

    private static ThreadLocal<DateFormat> simpleDateThread = new ThreadLocal<DateFormat>();
    private static ThreadLocal<DateFormat> simpleDateWithDashThread = new ThreadLocal<DateFormat>();
    private static ThreadLocal<DateFormat> dateTimeThread = new ThreadLocal<DateFormat>();
    private static ThreadLocal<DateFormat> dateTimeComplexThread = new ThreadLocal<DateFormat>();
    private static ThreadLocal<DateFormat> simpleDateWithDatabaseThread = new ThreadLocal<DateFormat>();
    private static ThreadLocal<DateFormat> simpleDateWithSlashThread = new ThreadLocal<DateFormat>();
    private static ThreadLocal<DateFormat> eightWinDateThread = new ThreadLocal<DateFormat>();
    
    // date format: yyyyMMdd
    public static DateFormat getSimpleDateFormat() {
    	DateFormat df = simpleDateThread.get();
    	if (df == null) {
    		df = new SimpleDateFormat(simple_date_format);
    		simpleDateThread.set(df);
    	}

    	return df;
    }

    public static Date parseEightWinDate (String dateStr) throws ParseException {
        return getEightWinFormat().parse(dateStr);
    }

    public static String formatEightWinDate (Date date) {
        return getEightWinFormat().format(date);
    }
    
    public static DateFormat getEightWinFormat() {
    	DateFormat df = eightWinDateThread.get();
    	if (df == null) {
    		df = new SimpleDateFormat(EIGHTWIN_FORMAT);
    		eightWinDateThread.set(df);
    	}

    	return df;
    }

    public static Date parseSimpleDate (String dateStr) throws ParseException {
        return getSimpleDateFormat().parse(dateStr);
    }

    public static String formatSimpleDate (Date date) {
        return getSimpleDateFormat().format(date);
    }

    // date format: yyyy.MM.dd
    public static DateFormat getSimpleDateFormatWithDot() {
    	DateFormat df = simpleDateThread.get();
    	if (df == null) {
    		df = new SimpleDateFormat(simple_date_format_dot);
    		simpleDateThread.set(df);
    	}
    	
    	return df;
    }

    public static Date parseSimpleDateWithDot (String dateStr) throws ParseException {
        return getSimpleDateFormatWithDot().parse(dateStr);
    }

    public static String formatSimpleDateWithDot (Date date) {
        return getSimpleDateFormatWithDot().format(date);
    }

    // date format: yyyy-MM-dd
    public static DateFormat getSimpleDateFormatWithDash() {
    	DateFormat df = simpleDateWithDashThread.get();
    	if (df == null) {
    		df = new SimpleDateFormat(simple_date_format_dash);
    		simpleDateWithDashThread.set(df);
    	}
    	
    	return df;
    }
    
    // date format: yyyy/MM/dd
    public static DateFormat getSimpleDateFormatWithSlash() {
    	DateFormat df = simpleDateWithSlashThread.get();
    	if (df == null) {
    		df = new SimpleDateFormat(simple_date_format_slash);
    		simpleDateWithSlashThread.set(df);
    	}
    	
    	return df;
    }

    public static Date parseSimpleDateWithDash (String dateStr) throws ParseException {
        return getSimpleDateFormatWithDash().parse(dateStr);
    }

    public static String formatSimpleDateWithDash (Date date) {
        return getSimpleDateFormatWithDash().format(date);
    }
    
    public static String formatSimpleDateWithSlash (Date date) {
        return getSimpleDateFormatWithSlash().format(date);
    }

    // date format: yyyy-MM-dd HH:mm:ss
    public static DateFormat getDateTimeDataBaseFormat() {
    	DateFormat df = simpleDateWithDatabaseThread.get();
    	if (df == null) {
    		df = new SimpleDateFormat(DATE_TIME_DATABASE_FORMAT);
    		simpleDateWithDatabaseThread.set(df);
    	}
    	
    	return df;
    }

    public static Date parseDateWithDataBase (String dateStr) throws ParseException {
        DateFormat format = getDateTimeDataBaseFormat();
        return format.parse(dateStr);
    }
    
    public static Date parseSimpleDateWithSlash (String dateStr) throws ParseException {
        return getSimpleDateFormatWithSlash().parse(dateStr);
    }

    public static String formatDateWithDataBase (Date date) {
        DateFormat format = getDateTimeDataBaseFormat();
        return format.format(date);
    }

    // date format: yyyy-MM-dd-HH:mm:ss
    public static DateFormat getDateTimeFormat() {
    	DateFormat df = dateTimeThread.get();
    	if (df == null) {
    		df = new SimpleDateFormat(date_time_format);
    		dateTimeThread.set(df);
    	}

    	return df; 
    }

    public static Date parseDateWithDash (String dateStr) throws ParseException {
        DateFormat format = getDateTimeFormat();
        return format.parse(dateStr);
    }

    public static String formatDateWithDash (Date date) {
        DateFormat format = getDateTimeFormat();
        return format.format(date);
    }

    // date format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
    public static DateFormat getDateTimeFormatComplex() {
    	DateFormat df = dateTimeComplexThread.get();
    	if (df == null) {
    		df = new SimpleDateFormat(date_time_complex_format);
    		dateTimeComplexThread.set(df);
    	}
    	
    	return df;
    }

    public static Date parseComplexDate (String dateStr) throws ParseException {
        return parseComplexDate(dateStr, GMT_TIMEZONE);
    }

    public static Date parseComplexDate (String dateStr, TimeZone zone) throws ParseException {
        DateFormat format = getDateTimeFormatComplex();
        format.setTimeZone(zone);
        return format.parse(dateStr);
    }

    public static String formatComplextSimpleDate (Date date) {
        DateFormat format = getDateTimeFormatComplex();
        return format.format(date);
    }

    public static Date parseComplexSimpleDate4GMT8TimeZone (String dateStr) {
        DateFormat format = getDateTimeFormatComplex();
        format.setTimeZone(GMT_PLUS8_TIMEZONE);
        try {
        	return format.parse(dateStr);
        }
        catch(ParseException ex) {
        }
        return null;
    }

    public static String formatComplexSimpleDate4GMT8TimeZone(Date date) {
        DateFormat format = getDateTimeFormatComplex();
        format.setTimeZone(GMT_PLUS8_TIMEZONE);
        return format.format(date);
    }

    public static Date parseComplexSimpleDate4PSTTimeZone (String dateStr) {
        DateFormat format = getDateTimeFormatComplex();
        format.setTimeZone(PST_TIMEZONE);
        try {
        	return format.parse(dateStr);
        }
        catch(ParseException ex) {
        }
        return null;
    }

    public static String formatComplexSimpleDate4PSTTimeZone(Date date) {
        DateFormat format = getDateTimeFormatComplex();
        format.setTimeZone(PST_TIMEZONE);
        return format.format(date);
    }

    /**
     * <p>Return format date string according to seconds
     * @param millionSeconds
     * @return X h X m Xs 
     */
    public static String getHMSFromMillionSeconds(long millionSeconds){
		StringBuffer time = new StringBuffer("");
		if ( millionSeconds == 0){
			return "0 ms";
		}
		
		if (!(millionSeconds / 3600000 == 0)) {
			time.append((millionSeconds / 3600000) + " h ");
			millionSeconds = millionSeconds % 3600000;
		}

		if (!(millionSeconds / 60000 == 0)) {
			time.append((millionSeconds / 60000) + " m ");
			millionSeconds = millionSeconds % 60000;
		}
    	
		if (!(millionSeconds / 1000 == 0)){
			time.append((millionSeconds / 1000) + " s ");
			millionSeconds = millionSeconds % 1000;
		}
		
		if (millionSeconds != 0){
			time.append(millionSeconds + " ms ");
		}
		
		return time.toString();
    }
    
    /**
     * Compare the day of two dates.
     *
     * @param current    the first date
     * @param another    the other date
     * @return
     */
    public static int compareDateDay (Date current, Date another) {
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(current);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(another);

        int currYear = calendar1.get(Calendar.YEAR);
        int anotherYear = calendar2.get(Calendar.YEAR);
        int currDay = calendar1.get(Calendar.DAY_OF_YEAR);
        int anotherDay = calendar2.get(Calendar.DAY_OF_YEAR);

        if (currYear > anotherYear) {
            return 1;
        } else if (currYear < anotherYear) {
            return -1;
        }

        if (currDay > anotherDay) {
            return 1;
        } else if  (currDay == anotherDay) {
            return 0;
        } else {
            return -1;
        }
    }

    /**
     * Get the beginning datetime of this week.
     *
     * @return
     */
    public static Date getStartDateTimeOfThisWeek (Date today) {
        Calendar cal = Calendar.getInstance();
        if (today != null) {
            cal.setTime(today);
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * Get the end datetime of this week.
     *
     * @return
     */
    public static Date getEndDateTimeOfThisWeek (Date today) {
        Calendar cal = Calendar.getInstance();
        if (today != null) {
            cal.setTime(today);
        }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTime();
    }

    /**
     * Get the date seven days ago.
     *
     * @return
     */
    public static Date getSevenDaysAgoDate (Date today) {
        Calendar cal = Calendar.getInstance();
        if (today != null) {
            cal.setTime(today);
        }
        cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) - 7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}
