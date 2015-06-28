package org.pelizzari.gis;

public class Box {

	Point nw, se;
	public Box(Point p1, Point p2) {
		float maxLat = p1.lat > p2.lat ? p1.lat : p2.lat;
		float maxLon = p1.lon > p2.lon ? p1.lon : p2.lon;
		float minLat = p1.lat < p2.lat ? p1.lat : p2.lat;
		float minLon = p1.lon < p2.lon ? p1.lon : p2.lon;
		this.nw = new Point(maxLat, minLon);
		this.se = new Point(minLat, maxLon);;
	}
	
	public float getMinLat() {
		return se.lat;
	}

	public float getMaxLat() {
		return nw.lat;
	}

	public float getMinLon() {
		return nw.lon;
	}

	public float getMaxLon() {
		return se.lon;
	}
	public boolean isWithinBox(Point p) {
		boolean inLat = (p.lat <= nw.lat) && (p.lat >= se.lat);
		boolean inLon = (p.lon >= nw.lon) && (p.lon <= se.lon);		
		return inLat && inLon;
	}

}
