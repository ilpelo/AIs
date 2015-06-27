package org.pelizzari.time;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class Timestamp {
	int ts; // Unix epoch
	
	public Timestamp(int ts) {
		this.ts = ts;
	}
	
	public String getISODatetime() {
		Date myDatetime = new Date((long)ts*1000);
		DateFormat isoDatetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String isoDatetimeStr = isoDatetimeFormat.format(myDatetime) + "Z";
		return isoDatetimeStr;
	}

	public int getTs() {
		return ts;
	}

	public void setTs(int ts) {
		this.ts = ts;
	}
	
}
