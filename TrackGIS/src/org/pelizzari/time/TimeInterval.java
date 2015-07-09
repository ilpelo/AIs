package org.pelizzari.time;

import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class TimeInterval {
	
	Timestamp startTs;
	Timestamp endTs; // start and end
	
	public TimeInterval(Timestamp startTs, Timestamp endTs) throws Exception {
		if(startTs.ts > endTs.ts) {
			throw new Exception("TimeInterval: start after end");
		}
		this.startTs = startTs;
		this.endTs = endTs;
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
	
	
}
	