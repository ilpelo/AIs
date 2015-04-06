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
		return (float) Math.sqrt(x*x + y*y) * 3440f; // * Earth radius in miles
	}
	
	public String toString() {
		return "["+String.format("%2.2f", lat)+","+String.format("%3.2f", lon)+"]";
	}

}
