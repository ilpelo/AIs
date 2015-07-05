package org.pelizzari.gis;

import java.util.ArrayList;
import java.util.List;

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
	
	public boolean equals(Object other) {
        if (!this.getClass().isInstance(other)) {
            return false;
        }
        Displacement otherDispl = (Displacement) other;
        float diffDeltaLat = Math.abs(deltaLat - otherDispl.deltaLat);
        float diffDeltaLon = Math.abs(deltaLon - otherDispl.deltaLon);        
        return (diffDeltaLat <= TOLERANCE) && (diffDeltaLon <= TOLERANCE);
	}
	
	/*
	 * Split in times+1 displacements with some randomness
	 */
	public DisplacementSequence split(int times) {
		DisplacementSequence splitDispl = new DisplacementSequence();
		Displacement displ = this;
		for (int i = 0; i < times; i++) {
			float latFraction = (float) Math.random();
			float lonFraction = (float) Math.random();
			Displacement displ1 = new Displacement(displ.deltaLat*latFraction, displ.deltaLon*lonFraction);
			splitDispl.add(displ1);
			displ = new Displacement(displ.deltaLat*(1-latFraction), displ.deltaLon*(1-lonFraction));
		}
		splitDispl.add(displ);
		return splitDispl;
	}
	
	public String toString() {
		String signedDelta = "%+2.2f";
		return "["+
				String.format(signedDelta, deltaLat)+","+
				String.format(signedDelta, deltaLon)+
				"]";
	}
}
