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

	public float squaredDistance(Point p) {
		float y = p.lat - lat;
		float x = p.lon - lon;
		return x*x + y*y;
	}

	public float distance(Point p) {
		return (float) Math.sqrt(squaredDistance(p));
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

	/**
	 * Return the intersection of a line passing by (p1, p2) and its perpendicular passing by this point.
	 * See http://stackoverflow.com/questions/1811549/perpendicular-on-a-line-from-a-given-point
	 * @param p
	 * @return
	 */
	public Point computeIntersectionOfPerpendicular(Point p1, Point p2) {		
		float x1 = p1.lon;
		float x2 = p2.lon;
		float y1 = p1.lat;
		float y2 = p2.lat;
		float x3 = lon;
		float y3 = lat;		
		float dx = (x2-x1);
		float dy = (y2-y1);		
		float k = ((y2-y1) * (x3-x1) - (x2-x1) * (y3-y1)) / (dy*dy + dx*dx);
		float x4 = x3 - k * dy;
		float y4 = y3 + k * dx;
		Point intersectionPoint = new Point(y4, x4);
		return intersectionPoint;
	}
	
	public Displacement computeDisplacement(Point destPoint) {
		float deltaLat = destPoint.lat - lat;
		float deltaLon = destPoint.lon - lon;
		Displacement d = new Displacement(deltaLat, deltaLon);
		return d;		
	}


	/**
	 * Return if the point is located ON the segment or not.
	 * http://stackoverflow.com/questions/328107/how-can-you-determine-a-point-is-between-two-other-points-on-a-line-segment
	 * @param p1
	 * @param p2
	 * @return
	 */
	public boolean isOnSegment(Point p1, Point p2) {
		float dotProduct = (lon - p1.lon) * (p2.lon - p1.lon) + (lat - p1.lat) * (p2.lat - p1.lat);
		if(dotProduct < 0) { return false; }

		float squaredSegmentLength = p1.squaredDistance(p2);
		if(dotProduct > squaredSegmentLength) { return false; }
		
		return true;
	}	
	
	
	public String toString() {
		return "["+String.format("%2.2f", lat)+","+String.format("%3.2f", lon)+"]";
	}

}
