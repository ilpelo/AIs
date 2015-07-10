package org.pelizzari.time;

import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Timestamp {
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

	public long getTsMillisec() {
		return ts;
	}

	public void setTsMillisec(long tsMillisec) {
		this.ts = tsMillisec;
	}
	
}
