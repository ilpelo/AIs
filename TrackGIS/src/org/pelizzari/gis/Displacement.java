package org.pelizzari.gis;

public class Displacement {

	public float deltaLat, deltaLon;
	
	public Displacement(float deltaLat, float deltaLon) {
		this.deltaLat = deltaLat;
		this.deltaLon = deltaLon;
	}
	
	public Displacement sum(Displacement d) {
		float dLat = d.deltaLat + deltaLat;
		float dLon = d.deltaLon + deltaLon;
		Displacement sumD = new Displacement(dLat, dLon);
		return sumD;
	}
	
	public String toString() {
		return "["+
				String.format("+%2.2f", deltaLat)+","+
				String.format("+%3.2f", deltaLon)+
				"]";
	}

}
