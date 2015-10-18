package org.pelizzari.gis;

public class Box {

	Point nw, // north-west corner 
		  se, // south-east corner
		  poi; // point of interest (e.g. departure or arrival point)
	String name;
	
	public Box(float maxLat, float minLat, float maxLon, float minLon, String name, Point poi) {
		this.nw = new Point(maxLat, minLon);
		this.se = new Point(minLat, maxLon);
		this.name = name;
		this.poi = poi;
	}

	// nameless box
	public Box(Point p1, Point p2) {
		this(p1, p2, "", null);
	}
	
	// box without Point of Interest
	public Box(Point p1, Point p2, String name) {
		this(p1, p2, name, null);
	}	
		
	public Box(Point p1, Point p2, String name, Point poi) {
//		float maxLat = p1.lat > p2.lat ? p1.lat : p2.lat;
//		float maxLon = p1.lon > p2.lon ? p1.lon : p2.lon;
//		float minLat = p1.lat < p2.lat ? p1.lat : p2.lat;
//		float minLon = p1.lon < p2.lon ? p1.lon : p2.lon;
		this(p1.lat > p2.lat ? p1.lat : p2.lat,
			 p1.lat < p2.lat ? p1.lat : p2.lat,
			 p1.lon > p2.lon ? p1.lon : p2.lon,
			 p1.lon < p2.lon ? p1.lon : p2.lon,
			 name,
			 poi);		
	}
	
	// length is in degrees! (not km, miles, ...)
	public Box(Point center, float halfSideLength) {
		this(center.lat + halfSideLength,
			 center.lat - halfSideLength,
			 center.lon + halfSideLength,
			 center.lon - halfSideLength,
			 "",
			 null);		
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
	
	public Point getPoi() {
		return poi;
	}

	public void setPoi(Point poi) {
		this.poi = poi;
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
