package org.pelizzari.time;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimeInterval {
	
	Timestamp startTs;
	Timestamp endTs; // start and end
	

	public TimeInterval() {
	}	
	
	public TimeInterval(Timestamp startTs, Timestamp endTs) throws Exception {
		if(startTs.ts > endTs.ts) {
			throw new Exception("TimeInterval: start after end");
		}
		this.startTs = startTs;
		this.endTs = endTs;
	}
	
	public TimeInterval(Timestamp startTs, int durationInDays) throws Exception {
		this();
		Timestamp startTS1 = startTs;
		Timestamp startTS2 = null;
		try {
			startTS2 = new Timestamp(startTS1.getTsMillisec()+(long)durationInDays*3600*24*1000);
			this.startTs = startTS1;
			this.endTs = startTS2;
		} catch (Exception e) {
			System.err.println("error making interval");
			e.printStackTrace();
		}
	}
	
	public int getDurationInDays() {
		return (int)((endTs.ts-startTs.ts)/Timestamp.ONE_DAY_IN_MILLISEC);
	}
	
	public void shiftInterval(int days) {
		startTs.shiftTimestamp(days);
		endTs.shiftTimestamp(days);
	}
	
	public boolean isWithinInterval(Timestamp ts) {
		boolean within = (ts.ts <= endTs.ts) && (ts.ts >= startTs.ts);
		return within;
	}

	public Timestamp getStartTs() {
		return startTs;
	}

	public String getStartTsISO() {
		return startTs.getISODatetime();
	}

	
	public void setStartTs(Timestamp startTs) {
		this.startTs = startTs;
	}

	public Timestamp getEndTs() {
		return endTs;
	}

	public String getEndTsISO() {
		return endTs.getISODatetime();
	}
	
	public void setEndTs(Timestamp endTs) {
		this.endTs = endTs;
	}
	
	public String toString() {
		return startTs + " - " + endTs;
	}
	
	
}
	