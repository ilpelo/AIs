package org.pelizzari.ship;

import org.pelizzari.gis.*;

public class ShipPosition {
	Point point;
	Timestamp ts;
	
	public ShipPosition(Point point, Timestamp ts) {
		this.point = point;
		this.ts = ts;
	}
	
	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public Timestamp getTs() {
		return ts;
	}

	public void setTs(Timestamp ts) {
		this.ts = ts;
	}

	/*
	 * Returns the ChangeOfCourse needed to reach the position pos from the current position.
	 */
	public ChangeOfCourse computeChangeOfCourse(ShipPosition pos) throws Exception {
		float course = -1;
		float lonDiff = pos.point.lon - point.lon;
		float latDiff = pos.point.lat - point.lat;
		int duration = pos.ts.getTs() - ts.getTs();
		if(Math.abs(lonDiff) < ChangeOfCourse.COURSE_PRECISION &&
		   Math.abs(latDiff) < ChangeOfCourse.COURSE_PRECISION) {
			return new ChangeOfCourse(0, duration);
		} else
		if(Math.abs(lonDiff) < ChangeOfCourse.COURSE_PRECISION) {
			course = latDiff > 0 ? 0 : 180; 
		} else
		if(Math.abs(latDiff) < ChangeOfCourse.COURSE_PRECISION) {
			course = lonDiff > 0 ? 90 : 270; 
		} else {
			if(lonDiff > 0) {
				course = 90f - (float) Math.toDegrees(Math.atan(latDiff/lonDiff));				
			} else {
				course = 270f + (float) Math.toDegrees(Math.atan(latDiff/-lonDiff));								
			}
		}
		return new ChangeOfCourse(course, duration);
	}
	
	public String toString() {
		return "(" + point + ", " + ts.getISODatetime() + ")";
	}
}
