package org.pelizzari.ship;

import org.pelizzari.gis.*;
import org.pelizzari.time.Timestamp;

public class ShipPosition {
	int index;
	Point point;
	Timestamp ts;
	
	public ShipPosition(Point point, Timestamp ts) {
		index = -1;
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

	public void setTs(long tsEpochInMillisec) {
		this.ts = new Timestamp(tsEpochInMillisec);
	}	
	
	public int getIndex() {
		return index;
	}

	public void setIndex(long index) {
		this.index = (int) index;
	}

	/*
	 * Checks if the difference of the slope between (p, p2) and (p, p3) is the same, with a given precision:
	 * slope(p, p2) - slope(p, p3) <= precision
	 */
	public boolean isAligned(ShipPosition p2, ShipPosition p3, float precision) {
		float dLat12 = point.lat - p2.point.lat;
		float dLat13 = point.lat - p3.point.lat;
		float dLon12 = point.lon - p2.point.lon;
		float dLon13 = point.lon - p3.point.lon;
		return Math.abs(dLat12 * dLon13 - dLat13 * dLon12) <= precision;
		
		//if(Math.signum(dLat12) != Math.signum(dLat13) || Math.signum(dLon12) != Math.signum(dLon13)) {
	}
	
	public ShipPosition computeNextPosition(ChangeOfCourse coc, float speedInKnots) {
		//float distanceInMinutesOfDegree = speedInKnots*(float)coc.duration/3600f; // knot=nm/h; duration:s
		float approxDistanceInDegree = coc.distance/60f;
		float dLat = (float) (approxDistanceInDegree * Math.cos(Math.toRadians(coc.course)));
		float dLon = (float) (approxDistanceInDegree * Math.sin(Math.toRadians(coc.course)));
		ShipPosition pos = new ShipPosition(new Point(point.lat + dLat, point.lon + dLon),
											new Timestamp(ts.getTsMillisec() + (int)(coc.distance/speedInKnots*3600000f)));
		return pos;
	}

	
	/**
	 * Compute the next position using the given displacement and timestamp 
	 * @param displ
	 * @param ts
	 * @return
	 */
	public ShipPosition computeNextPosition(Displacement displ, Timestamp ts) {
		float nextLat = point.lat + displ.deltaLat;
		float nextLon = point.lon + displ.deltaLon;
		Point nextPoint = new Point(nextLat, nextLon);
		ShipPosition nextPos = new ShipPosition(nextPoint, ts);
		return nextPos;
	}	
	
	/**
	 * Compute the next position using the given displacement; same timestamp as this position 
	 * @param displ
	 * @return
	 */
	public ShipPosition computeNextPosition(Displacement displ) {
		return computeNextPosition(displ, getTs());
	}

	/**
	 * Compute the next position using the given displacement; set the timestamp based on the speed
	 * @param displ
	 * @param speedInKnots
	 * @return
	 */
	public ShipPosition computeNextPosition(Displacement displ, float speedInKnots) {
		ShipPosition nextPos = computeNextPosition(displ);
		float distance = point.distanceInMiles(nextPos.getPoint());
		Timestamp nextTs = new Timestamp(ts.getTsMillisec() + (int)(distance/speedInKnots*3600000f));
		nextPos.setTs(nextTs);
		return nextPos;
	}
	
	/*
	 * Returns the ChangeOfCourse needed to reach the position pos from the current position.
	 */
	public ChangeOfCourse computeChangeOfCourse(ShipPosition pos) throws Exception {
		float course = -1;
		float lonDiff = pos.point.lon - point.lon;
		float latDiff = pos.point.lat - point.lat;
		//int duration = pos.ts.getTs() - ts.getTs();
		float distance = point.distanceInMiles(pos.point); // in nm
		if(Math.abs(lonDiff) < ChangeOfCourse.COURSE_PRECISION &&
		   Math.abs(latDiff) < ChangeOfCourse.COURSE_PRECISION) {
			course = 0;
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
		return new ChangeOfCourse(course, (int)distance);
	}
	
	/*
	 * Returns the Displacement needed to reach the position pos from the current position.
	 */
	public Displacement computeDisplacement(ShipPosition pos) throws Exception {
		float latDiff = pos.point.lat - point.lat;
		float lonDiff = pos.point.lon - point.lon;
		return new Displacement(latDiff, lonDiff);
	}
	
	public float getAverageSpeed(ShipPosition pos) { // in knots
		float distance = point.distanceInMiles(pos.point); // in nm
		float duration = (float) (ts.getTsMillisec() - pos.ts.getTsMillisec())/3600000f; // in hours
		return distance / duration; // knots
	}
	
	public String toString() {
		String indexStr = index == -1 ? "-" : ""+index;
		return "(" + indexStr + ", " + point + ", " + ts.getISODatetime() + "/" + ts.getTsMillisec() + ")";
	}
}
