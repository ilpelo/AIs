package org.pelizzari.gis;

public class Point {

	public float lat, lon, latRad, lonRad;
	public Point(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
		latRad = (float) Math.toRadians(lat);
		lonRad = (float) Math.toRadians(lon);
	}
	
	public float distanceInMiles(Point p) {
		// see http://www.movable-type.co.uk/scripts/latlong.html
		float y = p.latRad - latRad;
		float x = (p.lonRad - lonRad) * (float) Math.cos((p.latRad + latRad)/2);
		return (float) Math.sqrt(x*x + y*y) * 3410f; // * Earth radius in miles
	}
	
	
	/**
	 * See https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
	 * @param p1 segment fist point
	 * @param p2 segment second point
	 * @return
	 */
	public float approxSquaredDistanceToSegment(Point p1, Point p2) {
		float x1 = p1.lon;
		float x2 = p2.lon;
		float y1 = p1.lat;
		float y2 = p2.lat;
		float dx = (x2-x1);
		float dy = (y2-y1);		
		float num = dy*lon-dx*lat+x2*y1-y2*x1;
		float squaredSegmentLength = dx*dx + dy*dy;
		return num*num/squaredSegmentLength;
	}
	
	public String toString() {
		return "["+String.format("%2.2f", lat)+","+String.format("%3.2f", lon)+"]";
	}

}
