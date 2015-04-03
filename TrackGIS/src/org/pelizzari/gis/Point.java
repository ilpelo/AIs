package org.pelizzari.gis;

public class Point {

	public float lat, lon;
	public Point(float lat, float lon) {
		this.lat = lat;
		this.lon = lon;
	}
	
	public String toString() {
		return "["+String.format("%2.2f", lat)+","+String.format("%3.2f", lon)+"]";
	}

}
