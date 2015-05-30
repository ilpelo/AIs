package org.pelizzari.gis;

public class Displacement {
	
	final static float TOLERANCE = 0.1f; // tolerance in degree for 2 displacements to be equal

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
	
	public boolean equals(Object other) {
        if (!this.getClass().isInstance(other)) {
            return false;
        }
        Displacement otherDispl = (Displacement) other;
        float diffDeltaLat = Math.abs(deltaLat - otherDispl.deltaLat);
        float diffDeltaLon = Math.abs(deltaLon - otherDispl.deltaLon);        
        return (diffDeltaLat <= TOLERANCE) && (diffDeltaLon <= TOLERANCE);
	}

}
