package org.pelizzari.gis;

public class Box {

	Point nw, se;
	String name;
	
	public Box(float maxLat, float minLat, float maxLon, float minLon, String name) {
		this.nw = new Point(maxLat, minLon);
		this.se = new Point(minLat, maxLon);
		this.name = name;
	}

	// nameless box
	public Box(Point p1, Point p2) {
		this(p1, p2, "");
	}
	
	public Box(Point p1, Point p2, String name) {
//		float maxLat = p1.lat > p2.lat ? p1.lat : p2.lat;
//		float maxLon = p1.lon > p2.lon ? p1.lon : p2.lon;
//		float minLat = p1.lat < p2.lat ? p1.lat : p2.lat;
//		float minLon = p1.lon < p2.lon ? p1.lon : p2.lon;
		this(p1.lat > p2.lat ? p1.lat : p2.lat,
			 p1.lat < p2.lat ? p1.lat : p2.lat,
			 p1.lon > p2.lon ? p1.lon : p2.lon,
			 p1.lon < p2.lon ? p1.lon : p2.lon,
			 name);		
	}
	
	// length is in degrees! (not km, miles, ...)
	public Box(Point center, float halfSideLength) {
		this(center.lat + halfSideLength,
			 center.lat - halfSideLength,
			 center.lon + halfSideLength,
			 center.lon - halfSideLength,
			 "");		
	}
	
	public String getName() {
		return name;
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
	
	public String toString() {
		Point ne = new Point(nw.lat, se.lon);
		String s = "Box: NW = "+ nw + "; SE = " + se + 
				"; Lat side length = " + nw.distance(ne) +
				"; Lon side length = " + ne.distance(se);				
		return s;
	}

}
