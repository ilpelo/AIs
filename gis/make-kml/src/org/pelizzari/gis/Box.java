package org.pelizzari.gis;

public class Box {

	Point nw, se;
	public Box(Point nw, Point se) {
		this.nw = nw;
		this.se = se;
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
