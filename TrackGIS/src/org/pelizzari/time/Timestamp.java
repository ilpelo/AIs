package org.pelizzari.time;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Timestamp {
	public final static long ONE_DAY_IN_MILLISEC = 3600l*1000l*24l;
	
	long ts; // Unix epoch in millisec
	
	public Timestamp(long tsMillisec) {
		this.ts = tsMillisec;
	}

	public Timestamp(String datetimeISO) throws ParseException { // always use UTC
	    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    Date date = df.parse(datetimeISO);
	    ts = date.getTime();
	}
	
	public String getISODatetime() {
		Date myDatetime = new Date(ts);
		DateFormat isoDatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String isoDatetimeStr = isoDatetimeFormat.format(myDatetime) + "Z";
		return isoDatetimeStr;
	}

	public void shiftTimestamp(int days) {
		ts += days*ONE_DAY_IN_MILLISEC;
	}
	
	public long getTsMillisec() {
		return ts;
	}

	public void setTsMillisec(long tsMillisec) {
		this.ts = tsMillisec;
	}
	
	public String toString() {
		return getISODatetime();
	}
	
}
