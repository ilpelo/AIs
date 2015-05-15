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
		String signedDelta = "%+2.2f";
		return "["+
				String.format(signedDelta, deltaLat)+","+
				String.format(signedDelta, deltaLon)+
				"]";
	}

}
